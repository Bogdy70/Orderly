$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $repoRoot

function Get-DockerComposeCommand {
    try {
        docker compose version *> $null
        if ($LASTEXITCODE -eq 0) {
            return @("docker", "compose")
        }
    } catch {
    }

    try {
        docker-compose version *> $null
        if ($LASTEXITCODE -eq 0) {
            return @("docker-compose")
        }
    } catch {
    }

    throw "Docker Compose was not found. Install Docker Desktop, then reopen this terminal."
}

function Invoke-Compose {
    param([string[]]$Arguments)

    $compose = Get-DockerComposeCommand
    $composeArgs = @()
    if ($compose.Length -gt 1) {
        $composeArgs += $compose[1..($compose.Length - 1)]
    }
    $composeArgs += @("-f", "compose.yaml")
    $composeArgs += $Arguments

    & $compose[0] @composeArgs
}

function Wait-ForUrl {
    param(
        [Parameter(Mandatory = $true)][string]$Url,
        [Parameter(Mandatory = $true)][string]$Name,
        [int]$TimeoutSeconds = 240
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
                Write-Host "$Name is ready: $Url"
                return
            }
        } catch {
            Start-Sleep -Seconds 3
        }
    }

    throw "$Name did not become ready within $TimeoutSeconds seconds. Check logs with: docker compose -f compose.yaml logs $Name"
}

if (!(Test-Path ".env") -and (Test-Path ".env.example")) {
    Copy-Item ".env.example" ".env"
    Write-Host "Created .env from .env.example"
}

Write-Host "Starting Orderly with Docker Compose..."
Invoke-Compose -Arguments @("up", "--build", "-d")

$envValues = @{}
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#")) {
            return
        }
        $key, $value = $line -split "=", 2
        if ($key -and $null -ne $value) {
            $envValues[$key.Trim()] = $value.Trim()
        }
    }
}

$frontendPort = if ($envValues.ContainsKey("FRONTEND_PORT")) { $envValues["FRONTEND_PORT"] } else { "5173" }
$backendPort = if ($envValues.ContainsKey("BACKEND_PORT")) { $envValues["BACKEND_PORT"] } else { "8080" }
$keycloakPort = if ($envValues.ContainsKey("KEYCLOAK_PORT")) { $envValues["KEYCLOAK_PORT"] } else { "8081" }

Wait-ForUrl -Name "keycloak" -Url "http://localhost:$keycloakPort/realms/orderly"
Wait-ForUrl -Name "backend" -Url "http://localhost:$backendPort/v3/api-docs"
Wait-ForUrl -Name "frontend" -Url "http://localhost:$frontendPort"

Write-Host ""
Write-Host "Orderly is running."
Write-Host "Frontend:  http://localhost:$frontendPort"
Write-Host "Backend:   http://localhost:$backendPort/swagger-ui.html"
Write-Host "Keycloak:  http://localhost:$keycloakPort/admin"
Write-Host ""
Write-Host "Default dev users:"
Write-Host "  Keycloak admin: admin / admin"
Write-Host "  Demo user:      demo / demo"
