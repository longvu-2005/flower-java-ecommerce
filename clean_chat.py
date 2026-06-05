import os
import re

files = ['admin.jsp', 'ManagerProduct.jsp', 'ManagerCustomer.jsp', 'manageOrders.jsp', 'manageCategory.jsp', 'adminOrderDetail.jsp', 'AdminProductReviews.jsp', 'manageCoupon.jsp']
base_dir = 'd:/Học PRJ301/Nhom_5/NHOM6/web/view/'

for f in files:
    path = os.path.join(base_dir, f)
    if not os.path.exists(path):
        continue
    with open(path, 'r', encoding='utf-8') as file:
        content = file.read()
    
    # 1. Replace the HTML chunk. It starts with <div id="content-chat" style="display: none;">
    # It ends with the closing div of content-chat. It has chat-layout, chat-sidebar, chat-main...
    # We can use regex to match the exact block.
    # The block ends after <div class="chat-input-area"> ... </div> ... </div> ... </div>
    pattern_html = re.compile(r'<div id=\"content-chat\" style=\"display: none;\">.*?<div class=\"chat-input-area\">.*?</div>\s*</div>\s*</div>\s*</div>', re.DOTALL)
    content = pattern_html.sub('<jsp:include page="admin_chat_layout.jsp" />', content)

    # 2. Replace the JS chunk.
    # It starts with 'let currentChatClientId = null;'
    # It ends with 'document.getElementById('admin-msg-input').addEventListener('keypress', function(e) { if (e.key === 'Enter') sendAdminMessage(); });'
    pattern_js = re.compile(r'let currentChatClientId = null;.*?document\.getElementById\(\'admin-msg-input\'\)\.addEventListener\(\'keypress\', function\(e\) \{ if \(e\.key === \'Enter\'\) sendAdminMessage\(\); \}\);', re.DOTALL)
    content = pattern_js.sub('', content)

    with open(path, 'w', encoding='utf-8') as file:
        file.write(content)
    print(f'Processed {f}')
