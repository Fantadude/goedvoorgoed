#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Downloads the latest successful APK artifact from GitHub Actions and creates a draft release.

.DESCRIPTION
    This script finds the latest successful Android build workflow run,
    downloads the APK artifacts, and creates a draft GitHub release with them attached.

.PARAMETER ReleaseTag
    Optional. The release tag (e.g., "v1.0.0"). If not provided, auto-generated as "vYYYY.MM.DD-run#".

.PARAMETER ReleaseName
    Optional. The release name. If not provided, auto-generated.

.PARAMETER GitHubToken
    Required. Your GitHub Personal Access Token with 'repo' permissions.

.EXAMPLE
    .\create-draft-release.ps1 -GitHubToken "ghp_xxxxxxxxxxxx"

.EXAMPLE
    .\create-draft-release.ps1 -GitHubToken "ghp_xxxxxxxxxxxx" -ReleaseTag "v2.0.0" -ReleaseName "Version 2.0"
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory=$false)]
    [string]$ReleaseTag,

    [Parameter(Mandatory=$false)]
    [string]$ReleaseName,

    [Parameter(Mandatory=$true)]
    [string]$GitHubToken
)

$ErrorActionPreference = "Stop"

# Configuration
$repoOwner = "Fantadude"
$repoName = "goedvoorgoed"
$workflowFile = "build-android.yml"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "🚀 GitHub Release Creator" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# Validate token format
if (-not $GitHubToken.StartsWith("ghp_") -and -not $GitHubToken.StartsWith("github_pat_")) {
    Write-Warning "Token format may be invalid. Expected ghp_* or github_pat_* format."
}

$headers = @{
    "Authorization" = "Bearer $GitHubToken"
    "Accept" = "application/vnd.github.v3+json"
}

# Step 1: Get latest successful workflow run
Write-Host "`n📋 Step 1: Finding latest successful workflow run..." -ForegroundColor Yellow

try {
    $apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/actions/workflows/$workflowFile/runs?status=success&per_page=1"
    Write-Host "   API: $apiUrl" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri $apiUrl -Headers $headers -Method Get

    if ($response.total_count -eq 0) {
        throw "No successful workflow runs found!"
    }

    $latestRun = $response.workflow_runs[0]
    $runId = $latestRun.id
    $runDate = [DateTime]$latestRun.created_at

    Write-Host "   ✅ Found run #$runId" -ForegroundColor Green
    Write-Host "      Date: $($runDate.ToString('yyyy-MM-dd HH:mm:ss'))" -ForegroundColor Gray
    Write-Host "      Commit: $($latestRun.head_sha.Substring(0,7))" -ForegroundColor Gray
}
catch {
    Write-Error "Failed to fetch workflow runs: $_"
    exit 1
}

# Step 2: Get artifacts for this run
Write-Host "`n📦 Step 2: Fetching artifacts from run #$runId..." -ForegroundColor Yellow

try {
    $artifactsUrl = "https://api.github.com/repos/$repoOwner/$repoName/actions/runs/$runId/artifacts"
    $artifactsResponse = Invoke-RestMethod -Uri $artifactsUrl -Headers $headers -Method Get

    $releaseArtifact = $artifactsResponse.artifacts | Where-Object { $_.name -eq "android-release-apk" }
    $debugArtifact = $artifactsResponse.artifacts | Where-Object { $_.name -eq "android-debug-apk" }

    if (-not $releaseArtifact -and -not $debugArtifact) {
        throw "No APK artifacts found in the latest run!"
    }

    Write-Host "   ✅ Found artifacts:" -ForegroundColor Green
    if ($releaseArtifact) { Write-Host "      - android-release-apk" -ForegroundColor Gray }
    if ($debugArtifact) { Write-Host "      - android-debug-apk" -ForegroundColor Gray }
}
catch {
    Write-Error "Failed to fetch artifacts: $_"
    exit 1
}

# Step 3: Download artifacts
Write-Host "`n⬇️ Step 3: Downloading artifacts..." -ForegroundColor Yellow

$tempDir = Join-Path $env:TEMP "goedvoorgoed-release-$(Get-Random)"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

$downloadedFiles = @()

function Download-Artifact($artifact, $type) {
    if (-not $artifact) { return }

    Write-Host "   Downloading $type APK..." -ForegroundColor Gray

    $zipPath = Join-Path $tempDir "$type.zip"
    $extractPath = Join-Path $tempDir $type

    # Download
    Invoke-RestMethod -Uri $artifact.archive_download_url -Headers $headers -Method Get -OutFile $zipPath

    # Extract
    Expand-Archive -Path $zipPath -DestinationPath $extractPath -Force

    # Find APK
    $apkFile = Get-ChildItem -Path $extractPath -Filter "*.apk" -Recurse | Select-Object -First 1

    if ($apkFile) {
        # Rename for clarity
        $newName = "goedvoorgoed-$type-$($runDate.ToString('yyyyMMdd')).apk"
        $destPath = Join-Path $tempDir $newName
        Move-Item -Path $apkFile.FullName -Destination $destPath -Force
        $script:downloadedFiles += $destPath
        Write-Host "   ✅ Saved as: $newName" -ForegroundColor Green
    }

    # Cleanup zip
    Remove-Item $zipPath -Force -ErrorAction SilentlyContinue
}

Download-Artifact $releaseArtifact "release"
Download-Artifact $debugArtifact "debug"

if ($downloadedFiles.Count -eq 0) {
    Write-Error "No APK files could be downloaded!"
    exit 1
}

# Step 4: Prepare release info
Write-Host "`n📝 Step 4: Preparing release information..." -ForegroundColor Yellow

if (-not $ReleaseTag) {
    $date = Get-Date -Format "yyyy.MM.dd"
    $ReleaseTag = "v$date-$($latestRun.run_number)"
}

if (-not $ReleaseName) {
    $ReleaseName = "Release $ReleaseTag - $(Get-Date -Format 'yyyy-MM-dd')"
}

$shortSha = $latestRun.head_sha.Substring(0, 7)

$releaseNotes = @"
## What's Included

This draft release was automatically created from the latest successful build.

- **Build Date:** $($runDate.ToString('yyyy-MM-dd HH:mm:ss')) UTC
- **Commit:** ``$shortSha``
- **Workflow Run:** [$runId](https://github.com/$repoOwner/$repoName/actions/runs/$runId)

## Artifacts

- ``android-release-apk``: Production-ready APK
- ``android-debug-apk``: Debug APK with development features

## Installation

1. Download the desired APK
2. Enable "Install from Unknown Sources" on your Android device
3. Install the APK

---
*This is a draft release. Please review before publishing.*
"@

Write-Host "   Tag: $ReleaseTag" -ForegroundColor Green
Write-Host "   Name: $ReleaseName" -ForegroundColor Green

# Step 5: Create draft release
Write-Host "`n🚀 Step 5: Creating draft release..." -ForegroundColor Yellow

try {
    # Check if tag exists
    try {
        Invoke-RestMethod -Uri "https://api.github.com/repos/$repoOwner/$repoName/git/refs/tags/$ReleaseTag" -Headers $headers -Method Get | Out-Null
        Write-Warning "Tag '$ReleaseTag' already exists. Will use existing tag."
    }
    catch {
        # Tag doesn't exist, create it
        Write-Host "   Creating new tag: $ReleaseTag" -ForegroundColor Gray

        $tagBody = @{
            ref = "refs/tags/$ReleaseTag"
            sha = $latestRun.head_sha
        } | ConvertTo-Json

        Invoke-RestMethod -Uri "https://api.github.com/repos/$repoOwner/$repoName/git/refs" -Headers $headers -Method Post -Body $tagBody | Out-Null
    }

    # Create release
    $releaseBody = @{
        tag_name = $ReleaseTag
        name = $ReleaseName
        body = $releaseNotes
        draft = $true
        prerelease = $false
    } | ConvertTo-Json

    $release = Invoke-RestMethod -Uri "https://api.github.com/repos/$repoOwner/$repoName/releases" -Headers $headers -Method Post -Body $releaseBody

    $releaseId = $release.id
    $uploadUrl = $release.upload_url -replace "{\\?name,label}", ""

    Write-Host "   ✅ Draft release created!" -ForegroundColor Green
    Write-Host "      URL: $($release.html_url)" -ForegroundColor Gray
}
catch {
    Write-Error "Failed to create release: $_"
    exit 1
}

# Step 6: Upload assets
Write-Host "`n📤 Step 6: Uploading APK assets..." -ForegroundColor Yellow

foreach ($file in $downloadedFiles) {
    $fileName = Split-Path $file -Leaf
    Write-Host "   Uploading: $fileName" -ForegroundColor Gray

    try {
        $contentType = "application/vnd.android.package-archive"
        $uploadHeaders = @{
            "Authorization" = "Bearer $GitHubToken"
            "Content-Type" = $contentType
        }

        $assetUrl = "$uploadUrl?name=$fileName"
        Invoke-RestMethod -Uri $assetUrl -Headers $uploadHeaders -Method Post -InFile $file | Out-Null

        Write-Host "   ✅ Uploaded successfully" -ForegroundColor Green
    }
    catch {
        Write-Warning "Failed to upload $fileName : $_"
    }
}

# Cleanup
Write-Host "`n🧹 Cleaning up temporary files..." -ForegroundColor Gray
Remove-Item $tempDir -Recurse -Force -ErrorAction SilentlyContinue

# Summary
Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "✅ SUCCESS! Draft Release Created" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "🔗 URL: $($release.html_url)" -ForegroundColor White
Write-Host "🏷️  Tag: $ReleaseTag" -ForegroundColor White
Write-Host "📱 APKs attached: $($downloadedFiles.Count)" -ForegroundColor White
Write-Host "`n📋 Next Steps:" -ForegroundColor Yellow
Write-Host "   1. Visit the release URL above" -ForegroundColor Gray
Write-Host "   2. Review the release notes and assets" -ForegroundColor Gray
Write-Host "   3. Click 'Publish release' when ready" -ForegroundColor Gray
Write-Host "==========================================" -ForegroundColor Cyan
