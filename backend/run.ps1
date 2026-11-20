param(
    [ValidateSet("jar", "maven")]
    [string]$Mode = "jar"
)

$envFile = Join-Path $PSScriptRoot ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $parts = $line.Split("=", 2)
            [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), "Process")
        }
    }
}

Set-Location $PSScriptRoot
if ($Mode -eq "maven") {
    & .\mvnw.cmd spring-boot:run
} else {
    & 'E:\java\bin\java.exe' -jar .\target\parqueadero-0.0.1-SNAPSHOT.jar
}
