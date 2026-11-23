// Small progressive enhancements: mobile menu, confirm dialogs, flash messages.
(function () {
  var btn = document.getElementById('menu-btn');
  var side = document.getElementById('side');
  if (btn && side) {
    btn.addEventListener('click', function () {
      var open = side.classList.toggle('is-open');
      document.body.classList.toggle('menu-open', open);
      btn.setAttribute('aria-expanded', open ? 'true' : 'false');
    });
    document.addEventListener('click', function (e) {
      if (document.body.classList.contains('menu-open') && !side.contains(e.target) && !btn.contains(e.target)) {
        side.classList.remove('is-open'); document.body.classList.remove('menu-open');
      }
    });
  }
  document.addEventListener('submit', function (e) {
    var msg = e.target.getAttribute && e.target.getAttribute('data-confirm');
    if (msg && !window.confirm(msg)) e.preventDefault();
  });
  var flash = document.querySelector('.flash-success, .flash-info');
  if (flash) setTimeout(function () { flash.classList.add('is-gone'); }, 6000);
})();
