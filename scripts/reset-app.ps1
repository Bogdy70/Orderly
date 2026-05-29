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

Write-Host "Stopping Orderly and deleting local Docker volumes..."
$downArgs = @()
$downArgs += $composeArgs
$downArgs += @("-f", "compose.yaml", "down", "-v")
& $compose[0] @downArgs

Write-Host ""
Write-Host "Local data deleted. Run scripts/start-app.ps1 to rebuild and start fresh."
