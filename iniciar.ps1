# Execute no PowerShell, a partir da pasta raiz, depois de preencher o .env.
$ErrorActionPreference = 'Stop'
if (-not (Test-Path '.env')) { throw 'Copie .env.example para .env e preencha a conexao Neon.' }
Get-Content '.env' | ForEach-Object {
    $linha = $_.Trim()
    if ($linha -and -not $linha.StartsWith('#')) {
        $partes = $linha -split '=', 2
        if ($partes.Count -eq 2) {
            [Environment]::SetEnvironmentVariable($partes[0].Trim(), $partes[1].Trim(), 'Process')
        }
    }
}
mvn spring-boot:run
