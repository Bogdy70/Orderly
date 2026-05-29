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

$compose = Get-DockerComposeCommand
$composeArgs = @()
if ($compose.Length -gt 1) {
    $composeArgs += $compose[1..($compose.Length - 1)]
}
$composeArgs += @("-f", "compose.yaml", "down")

Write-Host "Stopping Orderly containers without deleting data..."
& $compose[0] @composeArgs

Write-Host ""
Write-Host "Stopped. Local database and Keycloak data are preserved."
Write-Host "To delete all local data, run: docker compose -f compose.yaml down -v"
