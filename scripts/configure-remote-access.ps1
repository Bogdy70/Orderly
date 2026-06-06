param(
    [Parameter(Mandatory = $true)]
    [string]$ServerHost
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $repoRoot

function Assert-ValidServerHost {
    param([string]$HostValue)

    if ([string]::IsNullOrWhiteSpace($HostValue)) {
        throw "ServerHost cannot be empty."
    }

    if ($HostValue -match "^https?://") {
        throw "ServerHost must be only a hostname or IP address, without http:// or https://."
    }

    if ($HostValue -match "[:/\\\s]") {
        throw "ServerHost must not contain ports, slashes, backslashes, or spaces."
    }

    if ($HostValue -notmatch "^[A-Za-z0-9][A-Za-z0-9.-]*$") {
        throw "ServerHost contains unsupported characters. Use a LAN IPv4 address, Tailscale IPv4 address, or hostname."
    }
}

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
    param(
        [Parameter(Mandatory = $true)][string[]]$Arguments,
        [switch]$CaptureOutput
    )

    $compose = Get-DockerComposeCommand
    $composeArgs = @()
    if ($compose.Length -gt 1) {
        $composeArgs += $compose[1..($compose.Length - 1)]
    }
    $composeArgs += @("-f", "compose.yaml")
    $composeArgs += $Arguments

    if ($CaptureOutput) {
        $output = & $compose[0] @composeArgs 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw ($output -join "`n")
        }
        return $output
    }

    & $compose[0] @composeArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose command failed: $($Arguments -join ' ')"
    }
}

function Ensure-EnvFile {
    if (!(Test-Path ".env")) {
        if (Test-Path ".env.example") {
            Copy-Item ".env.example" ".env"
            Write-Host "Created .env from .env.example"
        } else {
            New-Item -Path ".env" -ItemType File | Out-Null
            Write-Host "Created empty .env"
        }
    }
}

function Set-EnvValue {
    param(
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][string]$Value
    )

    Ensure-EnvFile

    $lines = @(Get-Content ".env")
    $found = $false
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match "^\s*$([regex]::Escape($Name))\s*=") {
            $lines[$i] = "$Name=$Value"
            $found = $true
            break
        }
    }

    if (!$found) {
        $lines += "$Name=$Value"
    }

    Set-Content -Path ".env" -Value $lines -Encoding ascii
}

function Read-EnvFile {
    $values = @{}
    if (!(Test-Path ".env")) {
        return $values
    }

    Get-Content ".env" | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#")) {
            return
        }
        $key, $value = $line -split "=", 2
        if ($key -and $null -ne $value) {
            $values[$key.Trim()] = $value.Trim()
        }
    }

    return $values
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

    throw "$Name did not become ready within $TimeoutSeconds seconds."
}

function Invoke-KeycloakAdmin {
    param([Parameter(Mandatory = $true)][string[]]$Arguments)

    return Invoke-Compose -CaptureOutput -Arguments (@("exec", "-T", "keycloak", "/opt/keycloak/bin/kcadm.sh") + $Arguments)
}

function Add-UniqueValues {
    param(
        $Existing,
        [string[]]$ValuesToAdd
    )

    $result = New-Object System.Collections.Generic.List[string]
    foreach ($value in @($Existing)) {
        if ($null -ne $value -and $value -ne "" -and !$result.Contains([string]$value)) {
            $result.Add([string]$value)
        }
    }

    foreach ($value in $ValuesToAdd) {
        if ($null -ne $value -and $value -ne "" -and !$result.Contains($value)) {
            $result.Add($value)
        }
    }

    return @($result)
}

function Get-EnvValueOrDefault {
    param(
        [hashtable]$Values,
        [string]$Name,
        [string]$Default
    )

    if ($Values.ContainsKey($Name) -and $Values[$Name]) {
        return $Values[$Name]
    }

    return $Default
}

function Get-KeycloakClient {
    param(
        [string]$Realm,
        [string]$ClientId
    )

    $json = (Invoke-KeycloakAdmin -Arguments @("get", "clients", "-r", $Realm, "-q", "clientId=$ClientId")) -join "`n"
    $clients = @($json | ConvertFrom-Json)
    if ($clients.Count -eq 0) {
        return $null
    }

    $clientUuid = $clients[0].id
    $clientJson = (Invoke-KeycloakAdmin -Arguments @("get", "clients/$clientUuid", "-r", $Realm)) -join "`n"
    return ($clientJson | ConvertFrom-Json)
}

function Save-KeycloakClient {
    param(
        [string]$Realm,
        $Client
    )

    $tempFile = Join-Path ([System.IO.Path]::GetTempPath()) "orderly-keycloak-client.json"
    $Client | ConvertTo-Json -Depth 40 | Set-Content -Path $tempFile -Encoding utf8

    docker cp $tempFile "orderly-keycloak:/tmp/orderly-keycloak-client.json" | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to copy updated Keycloak client JSON into the Keycloak container."
    }

    Invoke-KeycloakAdmin -Arguments @("update", "clients/$($Client.id)", "-r", $Realm, "-f", "/tmp/orderly-keycloak-client.json") | Out-Null
    Remove-Item $tempFile -Force -ErrorAction SilentlyContinue
}

function New-FrontendClient {
    param([string]$ClientId)

    return [pscustomobject]@{
        clientId = $ClientId
        name = "Orderly Frontend"
        enabled = $true
        protocol = "openid-connect"
        publicClient = $true
        standardFlowEnabled = $true
        directAccessGrantsEnabled = $true
        implicitFlowEnabled = $false
        serviceAccountsEnabled = $false
        redirectUris = @()
        webOrigins = @()
        attributes = @{
            "pkce.code.challenge.method" = "S256"
        }
    }
}

Assert-ValidServerHost -HostValue $ServerHost
Set-EnvValue -Name "PUBLIC_HOST" -Value $ServerHost

$envValues = Read-EnvFile
$frontendPort = Get-EnvValueOrDefault -Values $envValues -Name "FRONTEND_PORT" -Default "5173"
$backendPort = Get-EnvValueOrDefault -Values $envValues -Name "BACKEND_PORT" -Default "8080"
$keycloakPort = Get-EnvValueOrDefault -Values $envValues -Name "KEYCLOAK_PORT" -Default "8081"
$realm = Get-EnvValueOrDefault -Values $envValues -Name "KEYCLOAK_REALM" -Default "orderly"
$clientId = Get-EnvValueOrDefault -Values $envValues -Name "KEYCLOAK_FRONTEND_CLIENT_ID" -Default "orderly-frontend"
$adminUser = Get-EnvValueOrDefault -Values $envValues -Name "KEYCLOAK_ADMIN" -Default "admin"
$adminPassword = Get-EnvValueOrDefault -Values $envValues -Name "KEYCLOAK_ADMIN_PASSWORD" -Default "admin"

Write-Host "Configured PUBLIC_HOST=$ServerHost in .env"
Write-Host "Rebuilding/recreating containers so browser-facing URLs use PUBLIC_HOST..."
Invoke-Compose -Arguments @("up", "--build", "-d")

Wait-ForUrl -Name "keycloak" -Url "http://localhost:$keycloakPort/realms/$realm"

Write-Host "Updating Keycloak client '$clientId' redirects and web origins..."
Invoke-KeycloakAdmin -Arguments @(
    "config", "credentials",
    "--server", "http://127.0.0.1:8080",
    "--realm", "master",
    "--user", $adminUser,
    "--password", $adminPassword
) | Out-Null

$client = Get-KeycloakClient -Realm $realm -ClientId $clientId
if ($null -eq $client) {
    Write-Host "Client '$clientId' was not found. Creating it..."
    $client = New-FrontendClient -ClientId $clientId
    $tempFile = Join-Path ([System.IO.Path]::GetTempPath()) "orderly-new-keycloak-client.json"
    $client | ConvertTo-Json -Depth 40 | Set-Content -Path $tempFile -Encoding utf8
    docker cp $tempFile "orderly-keycloak:/tmp/orderly-new-keycloak-client.json" | Out-Null
    Invoke-KeycloakAdmin -Arguments @("create", "clients", "-r", $realm, "-f", "/tmp/orderly-new-keycloak-client.json") | Out-Null
    Remove-Item $tempFile -Force -ErrorAction SilentlyContinue
    $client = Get-KeycloakClient -Realm $realm -ClientId $clientId
}

$frontendOrigins = @(
    "http://localhost:$frontendPort",
    "http://127.0.0.1:$frontendPort",
    "http://$ServerHost`:$frontendPort"
)

$backendOrigins = @(
    "http://localhost:$backendPort",
    "http://127.0.0.1:$backendPort",
    "http://$ServerHost`:$backendPort"
)

$redirectUris = @(
    "http://localhost:$frontendPort/*",
    "http://127.0.0.1:$frontendPort/*",
    "http://$ServerHost`:$frontendPort/*",
    "http://localhost:$backendPort/swagger-ui/oauth2-redirect.html",
    "http://127.0.0.1:$backendPort/swagger-ui/oauth2-redirect.html",
    "http://$ServerHost`:$backendPort/swagger-ui/oauth2-redirect.html"
)

$client.redirectUris = Add-UniqueValues -Existing $client.redirectUris -ValuesToAdd $redirectUris
$client.webOrigins = Add-UniqueValues -Existing $client.webOrigins -ValuesToAdd ($frontendOrigins + $backendOrigins)

Save-KeycloakClient -Realm $realm -Client $client

Write-Host ""
Write-Host "Remote access configuration complete."
Write-Host "Frontend: http://$ServerHost`:$frontendPort"
Write-Host "Backend:  http://$ServerHost`:$backendPort/swagger-ui.html"
Write-Host "Keycloak: http://$ServerHost`:$keycloakPort/admin"
Write-Host ""
Write-Host "Run this again with -ServerHost localhost to rebuild for local-only browser URLs."
