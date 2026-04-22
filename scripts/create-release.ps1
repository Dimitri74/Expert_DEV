param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path,
    [string]$RuntimePath,
    [string]$Version,
    [string]$OutputDir,
    [switch]$SkipBuild,
    [switch]$IncludeJar
)

$ErrorActionPreference = "Stop"

function Get-ProjectVersion {
    param([string]$PomPath)

    [xml]$pomXml = Get-Content -Path $PomPath
    $namespaceUri = $pomXml.DocumentElement.NamespaceURI
    $ns = New-Object System.Xml.XmlNamespaceManager($pomXml.NameTable)
    $ns.AddNamespace("m", $namespaceUri)

    $versionNode = $pomXml.SelectSingleNode("/m:project/m:version", $ns)
    if (-not $versionNode -or [string]::IsNullOrWhiteSpace($versionNode.InnerText)) {
        throw "Nao foi possivel ler a versao no arquivo pom.xml"
    }

    return $versionNode.InnerText.Trim()
}

if (-not (Test-Path -Path $ProjectRoot)) {
    throw "ProjectRoot invalido: $ProjectRoot"
}

$pomPath = Join-Path $ProjectRoot "pom.xml"
if (-not (Test-Path -Path $pomPath)) {
    throw "pom.xml nao encontrado em: $ProjectRoot"
}

if ([string]::IsNullOrWhiteSpace($Version)) {
    $Version = Get-ProjectVersion -PomPath $pomPath
}

if ([string]::IsNullOrWhiteSpace($RuntimePath)) {
    $RuntimePath = Join-Path $ProjectRoot "jre8"
}

if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $ProjectRoot "target\release"
}

$exePath = Join-Path $ProjectRoot "target\ExpertDev.exe"
$jarPath = Join-Path $ProjectRoot ("target\expert-dev-{0}.jar" -f $Version)
$stageDir = Join-Path $OutputDir ("ExpertDev-{0}-win64" -f $Version)
$zipPath = Join-Path $OutputDir ("ExpertDev-{0}-win64-portable.zip" -f $Version)

if (-not $SkipBuild) {
    Push-Location $ProjectRoot
    try {
        & mvn -DskipTests package
    }
    finally {
        Pop-Location
    }
}

if (-not (Test-Path -Path $exePath)) {
    throw "Executavel nao encontrado: $exePath"
}

if (-not (Test-Path -Path $RuntimePath)) {
    throw "Runtime nao encontrado: $RuntimePath"
}

if (Test-Path -Path $stageDir) {
    Remove-Item -Path $stageDir -Recurse -Force
}
New-Item -Path $stageDir -ItemType Directory -Force | Out-Null

Copy-Item -Path $exePath -Destination (Join-Path $stageDir "ExpertDev.exe") -Force
Copy-Item -Path $RuntimePath -Destination (Join-Path $stageDir "jre8") -Recurse -Force

$rootConfig = Join-Path $ProjectRoot "expertdev.properties"
if (Test-Path -Path $rootConfig) {
    Copy-Item -Path $rootConfig -Destination (Join-Path $stageDir "expertdev.properties") -Force
}

$readmePath = Join-Path $ProjectRoot "README.md"
if (Test-Path -Path $readmePath) {
    Copy-Item -Path $readmePath -Destination (Join-Path $stageDir "README.md") -Force
}

if ($IncludeJar -and (Test-Path -Path $jarPath)) {
    Copy-Item -Path $jarPath -Destination (Join-Path $stageDir ("expert-dev-{0}.jar" -f $Version)) -Force
}

$quickStartPath = Join-Path $stageDir "INICIAR_EXPERTDEV.txt"
@(
    "ExpertDev $Version - pacote portatil Windows"
    ""
    "1) Garanta que a pasta jre8 esteja ao lado do ExpertDev.exe"
    "2) Execute ExpertDev.exe com duplo clique"
    "3) Se o SmartScreen bloquear, escolha 'Mais informacoes' e depois 'Executar assim mesmo'"
) | Set-Content -Path $quickStartPath -Encoding ASCII

if (Test-Path -Path $zipPath) {
    Remove-Item -Path $zipPath -Force
}
Compress-Archive -Path (Join-Path $stageDir "*") -DestinationPath $zipPath -Force

Write-Output "Pacote gerado com sucesso: $zipPath"
Write-Output "Conteudo staged em: $stageDir"
