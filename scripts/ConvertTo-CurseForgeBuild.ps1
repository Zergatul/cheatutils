param
(
    [Parameter(ValueFromPipeline = $true)]
    [String]$JarPath
)

process
{
    $directory = [System.IO.Path]::GetDirectoryName($JarPath)
    $filename = [System.IO.Path]::GetFileNameWithoutExtension($JarPath)
    $extension = [System.IO.Path]::GetExtension($JarPath)
    $cfJarPath = [System.IO.Path]::Combine($directory, $filename + "-cf" + $extension)

    Copy-Item -Path $JarPath -Destination $cfJarPath

    $zip = [System.IO.Compression.ZipFile]::Open($cfJarPath, [System.IO.Compression.ZipArchiveMode]::Update)

    $entry = $zip.GetEntry('com/zergatul/cheatutils/scripting/modules/OsApiCurseForgeExcluded.class')
    if (-not $entry)
    {
        Write-Error 'Zip Entry not found'
        return
    }
    $entry.Delete()

    $entry = $zip.CreateEntry('curse-forge-build', [System.IO.Compression.CompressionLevel]::NoCompression)
    $entry.LastWriteTime = [DateTimeOffset]::new(1980, 1, 31, 23, 0, 0, [TimeSpan]::Zero)
    $stream = $entry.Open()
    $stream.Dispose()

    $zip.Dispose()
}