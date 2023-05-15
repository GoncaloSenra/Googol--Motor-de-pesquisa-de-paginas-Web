
class CustomNavBar extends HTMLElement {
    connectedCallback() {
        this.innerHTML = '<div class="container">\n' +
            '  <header class="d-flex flex-wrap justify-content-center py-3 mb-4 border-bottom">\n' +
            '    <a href="/search" class="d-flex align-items-center mb-3 mb-md-0 me-md-auto link-body-emphasis text-decoration-none">\n' +
            '      <svg class="bi me-2" width="40" height="32"><use xlink:href="#bootstrap"></use></svg>\n' +
            '      <span class="fs-4">GOOGOL</span>\n' +
            '    </a>\n' +
            '\n' +
            '    <ul class="nav nav-pills">\n' +
            '      <li class="nav-item"><a href="/search" class="nav-link active" aria-current="page">Search</a></li>\n' +
            '      <li class="nav-item"><a href="/admin" class="nav-link">Administration</a></li>\n' +
            '      <li class="nav-item"><a href="/index" class="nav-link">Index</a></li>\n' +
            '      <li class="nav-item"><a href="/pointers" class="nav-link">Pointer links</a></li>\n' +
            '    </ul>\n' +
            '  </header>\n' +
            '</div>'
    }
}

customElements.define('custom-nav', CustomNavBar)