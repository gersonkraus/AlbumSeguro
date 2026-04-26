$src = "\\wsl.localhost\Ubuntu\home\gerson\AlbumSeguro\android\app\src\main\java\com\familiaaco"
$dst = "C:\Projetos\AlbumSeguro\android\app\src\main\java\com\familiaaco"

$files = @(
    "MainActivity.kt",
    "ui\screens\VideoPlayerScreen.kt",
    "ui\screens\MediaViewerScreen.kt",
    "ui\screens\ChildDetailScreen.kt",
    "ui\screens\ChildAlbumScreen.kt",
    "ui\screens\MediaUploadScreen.kt",
    "ui\navigation\NavGraph.kt",
    "network\ApiService.kt",
    "repository\MediaRepository.kt",
    "viewmodel\MediaViewModel.kt",
    "viewmodel\ChildAlbumViewModel.kt",
    "data\models\DTOs.kt"
)

foreach ($f in $files) {
    $srcPath = Join-Path $src $f
    $dstPath = Join-Path $dst $f
    Copy-Item $srcPath -Destination $dstPath -Force
    Write-Host "OK: $f"
}

Write-Host "`nSincronizacao concluida. Abra o Android Studio e faca Build -> Make Project."
