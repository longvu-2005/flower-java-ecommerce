$files = @("admin.jsp", "ManagerProduct.jsp", "ManagerCustomer.jsp", "manageOrders.jsp", "manageCategory.jsp", "adminOrderDetail.jsp", "AdminProductReviews.jsp")
$base_dir = "d:/Học PRJ301/Nhom_5/NHOM6/web/view/"

foreach ($f in $files) {
    $path = Join-Path $base_dir $f
    if (Test-Path $path) {
        $corruptedContent = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
        
        # Convert from corrupted string to bytes using Windows-1252
        $encoding1252 = [System.Text.Encoding]::GetEncoding(1252)
        $bytes = $encoding1252.GetBytes($corruptedContent)

        # Convert bytes back to string using UTF-8
        $restoredContent = [System.Text.Encoding]::UTF8.GetString($bytes)

        # Save it back as UTF-8
        [System.IO.File]::WriteAllText($path, $restoredContent, [System.Text.Encoding]::UTF8)
        Write-Host "Restored encoding for $f"
    }
}
