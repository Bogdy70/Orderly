$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $repoRoot

function Get-LanAddresses {
    try {
        return @(Get-NetIPAddress -AddressFamily IPv4 |
            Where-Object {
                $octets = $_.IPAddress -split "\."
                $_.IPAddress -notlike "127.*" -and
                $_.IPAddress -notlike "169.254.*" -and
                !($octets.Count -eq 4 -and [int]$octets[0] -eq 100 -and [int]$octets[1] -ge 64 -and [int]$octets[1] -le 127) -and
                $_.AddressState -eq "Preferred" -and
                $_.InterfaceAlias -notmatch "docker|vEthernet|loopback|tailscale|zerotier|npcap"
            } |
            Select-Object -ExpandProperty IPAddress -Unique)
    } catch {
        return @()
    }
}

function Get-TailscaleAddresses {
    $tailscale = Get-Command tailscale -ErrorAction SilentlyContinue
    if (!$tailscale) {
        return [pscustomobject]@{
            Installed = $false
            IPv4 = @()
            Self = ""
        }
    }

    $ipv4 = @()
    $self = ""

    try {
        $ipv4 = @(tailscale ip -4 2>$null | Where-Object { $_ -and $_.Trim() -ne "" })
    } catch {
        $ipv4 = @()
    }

    try {
        $self = (tailscale status --self 2>$null | Select-Object -First 1)
    } catch {
        $self = ""
    }

    return [pscustomobject]@{
        Installed = $true
        IPv4 = $ipv4
        Self = $self
    }
}

$lanAddresses = @(Get-LanAddresses)
$tailscaleInfo = Get-TailscaleAddresses
$preferredHost = $null

Write-Host "Possible LAN addresses:"
if ($lanAddresses.Count -gt 0) {
    foreach ($address in $lanAddresses) {
        Write-Host "- $address"
    }
    $preferredHost = $lanAddresses[0]
} else {
    Write-Host "- No active LAN IPv4 address was detected."
}

Write-Host ""
Write-Host "Possible Tailscale address:"
if ($tailscaleInfo.Installed) {
    if ($tailscaleInfo.IPv4.Count -gt 0) {
        foreach ($address in $tailscaleInfo.IPv4) {
            Write-Host "- $address"
        }
        $preferredHost = $tailscaleInfo.IPv4[0]
    } else {
        Write-Host "- Tailscale is installed, but no Tailscale IPv4 address was detected."
    }

    if ($tailscaleInfo.Self) {
        Write-Host ""
        Write-Host "Tailscale self:"
        Write-Host $tailscaleInfo.Self
    }
} else {
    Write-Host "- Tailscale command not found. Install and authenticate Tailscale to use private remote access from another network."
}

if (!$preferredHost) {
    $preferredHost = "localhost"
}

Write-Host ""
Write-Host "Configure remote access with:"
Write-Host ".\scripts\configure-remote-access.ps1 -ServerHost $preferredHost"
Write-Host ""
Write-Host "Then open from the remote device:"
Write-Host "http://$preferredHost`:5173"
