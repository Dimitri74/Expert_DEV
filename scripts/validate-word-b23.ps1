param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path,
    [Parameter(Mandatory = $true)]
    [string]$DocxPath,
    [string]$DocPath,
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

function Invoke-Probe {
    param(
        [string]$PomPath,
        [string]$ArquivoWord
    )

    $cmd = @(
        "-f", $PomPath,
        "-DskipTests",
        "exec:java",
        "-Dexec.mainClass=br.com.expertdev.service.WordIngestionProbe",
        "-Dexec.args=$ArquivoWord"
    )

    & mvn @cmd | Out-Host
    return $LASTEXITCODE
}

function Set-WordConfig {
    param(
        [string]$ConfigPath,
        [bool]$ConversionEnabled,
        [string]$LibrePath,
        [bool]$FallbackEnabled
    )

    $lines = @()
    if (Test-Path -Path $ConfigPath) {
        $lines = Get-Content -Path $ConfigPath
    }

    $map = @{}
    foreach ($line in $lines) {
        if ($line -match "^\s*#" -or -not ($line -match "=")) {
            continue
        }
        $idx = $line.IndexOf("=")
        if ($idx -gt 0) {
            $k = $line.Substring(0, $idx).Trim()
            $v = $line.Substring($idx + 1)
            $map[$k] = $v
        }
    }

    $map["word.doc.conversion.enabled"] = $ConversionEnabled.ToString().ToLower()
    $map["word.libreoffice.path"] = $LibrePath
    $map["word.doc.fallback.to.direct.read"] = $FallbackEnabled.ToString().ToLower()

    $output = @()
    foreach ($k in $map.Keys | Sort-Object) {
        $output += "$k=$($map[$k])"
    }
    Set-Content -Path $ConfigPath -Value $output -Encoding ASCII
}

$pomPath = Join-Path $ProjectRoot "pom.xml"
$configPath = Join-Path $ProjectRoot "expertdev.properties"
$backupPath = "$configPath.b2_3_backup"

if (-not (Test-Path -Path $DocxPath)) {
    throw "DOCX nao encontrado: $DocxPath"
}
if ($DocPath -and -not (Test-Path -Path $DocPath)) {
    throw "DOC nao encontrado: $DocPath"
}
if (-not (Test-Path -Path $pomPath)) {
    throw "pom.xml nao encontrado em: $ProjectRoot"
}

$results = New-Object System.Collections.Generic.List[System.String]

if (-not $SkipBuild) {
    & mvn -f $pomPath -DskipTests compile
}

$hadBackup = $false
if (Test-Path -Path $configPath) {
    Copy-Item -Path $configPath -Destination $backupPath -Force
    $hadBackup = $true
}

try {
    Write-Output "=== Cenario 1: DOCX direto ==="
    $exit1 = Invoke-Probe -PomPath $pomPath -ArquivoWord $DocxPath
    $results.Add("Cenario 1 (DOCX): exit=$exit1")

    if (-not [string]::IsNullOrWhiteSpace($DocPath)) {
        Write-Output "=== Cenario 2: DOC com conversao habilitada e fallback habilitado ==="
        Set-WordConfig -ConfigPath $configPath -ConversionEnabled $true -LibrePath "" -FallbackEnabled $true
        $exit2 = Invoke-Probe -PomPath $pomPath -ArquivoWord $DocPath
        $results.Add("Cenario 2 (DOC conv on/fallback on): exit=$exit2")

        Write-Output "=== Cenario 3: DOC com conversao desabilitada e fallback habilitado ==="
        Set-WordConfig -ConfigPath $configPath -ConversionEnabled $false -LibrePath "" -FallbackEnabled $true
        $exit3 = Invoke-Probe -PomPath $pomPath -ArquivoWord $DocPath
        $results.Add("Cenario 3 (DOC conv off/fallback on): exit=$exit3")

        Write-Output "=== Cenario 4: DOC com conversao habilitada, fallback desabilitado e LibreOffice invalido ==="
        Set-WordConfig -ConfigPath $configPath -ConversionEnabled $true -LibrePath "C:\\invalido\\soffice.exe" -FallbackEnabled $false
        $exit4 = Invoke-Probe -PomPath $pomPath -ArquivoWord $DocPath
        $results.Add("Cenario 4 (DOC conv on/fallback off/libre invalido): exit=$exit4")
    } else {
        Write-Output "DOC nao informado: cenarios 2-4 foram pulados."
    }
}
finally {
    if ($hadBackup) {
        Move-Item -Path $backupPath -Destination $configPath -Force
    } elseif (Test-Path -Path $backupPath) {
        Remove-Item -Path $backupPath -Force
    }
}

Write-Output "=== Resumo B2.3 ==="
$results | ForEach-Object { Write-Output $_ }

