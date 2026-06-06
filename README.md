# Orderly

Orderly is a personal organization app with a React frontend, Spring Boot backend, PostgreSQL, and Keycloak authentication.

## Prerequisites

- Docker Desktop
- Windows PowerShell
- Optional: Tailscale on the server laptop and the remote device

The app is intended for local or private personal access. Do not expose it with router port forwarding, public tunnels, or a public domain.

## Mode A: Local-Only Development

From PowerShell:

```powershell
.\scripts\start-app.ps1
```

The script creates `.env` from `.env.example` if needed, builds the frontend and backend containers, starts PostgreSQL and Keycloak, then waits until the app is ready.

Access:

- Frontend: http://localhost:5173
- Backend Swagger: http://localhost:8080/swagger-ui.html
- Keycloak admin: http://localhost:8081/admin

Default dev credentials:

- Keycloak admin: `admin / admin`
- Demo user: `demo / demo`

If you previously configured a LAN or Tailscale host and want the frontend build to use localhost again:

```powershell
.\scripts\configure-remote-access.ps1 -ServerHost localhost
```

## Mode B: Same-LAN Access

Use this when another phone or laptop is on the same Wi-Fi/LAN as the server laptop.

```powershell
.\scripts\show-server-addresses.ps1
.\scripts\configure-remote-access.ps1 -ServerHost 192.168.1.50
```

Replace `192.168.1.50` with the LAN IPv4 address printed by the helper.

Then open this from the other device:

```text
http://192.168.1.50:5173
```

The remote browser should call:

- `http://192.168.1.50:8080`
- `http://192.168.1.50:8081`

It should not call its own `localhost`.

## Mode C: Private Access Through Tailscale

Install and authenticate Tailscale on:

- the server laptop;
- the remote phone or laptop.

Then run:

```powershell
.\scripts\show-server-addresses.ps1
.\scripts\configure-remote-access.ps1 -ServerHost <tailscale-hostname-or-ip>
```

Examples:

```powershell
.\scripts\configure-remote-access.ps1 -ServerHost 100.x.y.z
.\scripts\configure-remote-access.ps1 -ServerHost orderly-server.your-tailnet.ts.net
```

Open from the remote Tailscale device:

```text
http://<tailscale-hostname-or-ip>:5173
```

The Tailscale hostname is preferable when available because it is easier to reuse after restarts.

## What The Remote Access Script Does

`scripts/configure-remote-access.ps1`:

- validates the hostname/IP;
- creates `.env` from `.env.example` if needed;
- sets `PUBLIC_HOST=<hostname-or-ip>` while preserving unrelated `.env` values;
- rebuilds/recreates containers so Vite receives the correct browser-facing URLs;
- waits for Keycloak;
- updates the existing `orderly-frontend` Keycloak client inside the running container;
- keeps localhost redirects and origins;
- adds the configured host redirects and origins without duplicates.

The script is safe to run multiple times.

## Ports

Application ports that may be reachable from LAN/Tailscale:

- `5173`: frontend
- `8080`: backend
- `8081`: Keycloak

Database/admin-only ports are bound to localhost in Compose:

- `5433`: Orderly PostgreSQL
- `5050`: optional pgAdmin

The Keycloak PostgreSQL database is not published.

## Windows Firewall

Remote LAN or Tailscale access may require allowing inbound TCP traffic on the server laptop for:

- `5173`
- `8080`
- `8081`

Prefer Tailscale for private access. Avoid opening router ports or exposing Orderly directly to the public internet.

Optional narrowly scoped PowerShell rules:

```powershell
New-NetFirewallRule -DisplayName "Orderly Frontend 5173" -Direction Inbound -Action Allow -Protocol TCP -LocalPort 5173
New-NetFirewallRule -DisplayName "Orderly Backend 8080" -Direction Inbound -Action Allow -Protocol TCP -LocalPort 8080
New-NetFirewallRule -DisplayName "Orderly Keycloak 8081" -Direction Inbound -Action Allow -Protocol TCP -LocalPort 8081
```

Remove those rules later with:

```powershell
Remove-NetFirewallRule -DisplayName "Orderly Frontend 5173"
Remove-NetFirewallRule -DisplayName "Orderly Backend 8080"
Remove-NetFirewallRule -DisplayName "Orderly Keycloak 8081"
```

Do not run broad firewall commands unless you understand the network you are exposing the app to.

## Stop

```powershell
.\scripts\stop-app.ps1
```

This stops containers and preserves Docker volumes.

## Reset Local Data

```powershell
.\scripts\reset-app.ps1
```

This deletes local Docker volumes, including PostgreSQL and Keycloak data.

## Optional PgAdmin

```powershell
docker compose -f compose.yaml --profile tools up -d pgadmin
```

PgAdmin defaults to http://localhost:5050 with `admin@orderly.local / admin`. It is bound to localhost only.
