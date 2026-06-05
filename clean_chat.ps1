$files = @("admin.jsp", "ManagerProduct.jsp", "ManagerCustomer.jsp", "manageOrders.jsp", "manageCategory.jsp", "adminOrderDetail.jsp", "AdminProductReviews.jsp", "manageCoupon.jsp")
$base_dir = "d:/Học PRJ301/Nhom_5/NHOM6/web/view/"

foreach ($f in $files) {
    $path = Join-Path $base_dir $f
    if (Test-Path $path) {
        $content = Get-Content -Path $path -Raw
        
        $pattern_html = '(?s)<div id="content-chat" style="display: none;">.*?<div class="chat-input-area">.*?</div>\s*</div>\s*</div>\s*</div>'
        $content = [regex]::Replace($content, $pattern_html, '<jsp:include page="admin_chat_layout.jsp" />')

        $pattern_js = '(?s)let currentChatClientId = null;.*?document\.getElementById\(''admin-msg-input''\)\.addEventListener\(''keypress'', function\(e\) \{ if \(e\.key === ''Enter''\) sendAdminMessage\(\); \}\);'
        $content = [regex]::Replace($content, $pattern_js, '')

        Set-Content -Path $path -Value $content -Encoding UTF8
        Write-Host "Processed $f"
    }
}
