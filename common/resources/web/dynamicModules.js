function waitForModules(selector, callback, maxTries = 100, delay = 100) {
  let tries = 0;

  function check() {
    const container = document.querySelector(selector);
    const children = container?.querySelectorAll(".module-group");

    if (container && children && children.length > 0) {
      // Prüfe, ob mindestens ein Kind eine sinnvolle Höhe hat
      const hasValidHeight = Array.from(children).some(
        (el) => el.offsetHeight > 122,
      );

      if (hasValidHeight) {
        callback(container, children);
        return;
      }
    }

    if (tries < maxTries) {
      tries++;
      setTimeout(check, delay);
    } else {
      console.warn("Modules not found or not ready after waiting.");
    }
  }

  check();
}

function groupModules(container, modules) {
  const MAX_HEIGHT =
    window.innerHeight -
    document.getElementsByClassName("main-header")[0].offsetHeight -
    20;

  document
    .querySelectorAll(".groupedModule")
    .forEach((el) => el.replaceWith(...el.childNodes));
  const measured = Array.from(modules).map((el, index) => ({
    el,
    height: el.offsetHeight,
    index,
  }));

  measured.sort((a, b) => b.height - a.height);
  const used = new Set();

  for (let i = 0; i < measured.length; i++) {
    const big = measured[i];
    if (used.has(big.index)) continue;

    let paired = false;

    for (let j = measured.length - 1; j > i; j--) {
      const small = measured[j];
      if (used.has(small.index)) continue;

      if (big.height + small.height <= MAX_HEIGHT) {
        const wrapper = document.createElement("div");
        wrapper.className = "groupedModule";

        container.insertBefore(wrapper, big.el);
        wrapper.appendChild(big.el);
        wrapper.appendChild(small.el);

        used.add(big.index);
        used.add(small.index);
        paired = true;
        break;
      }
    }
  }
}

waitForModules(".modules", groupModules);

let resizeTimeout;

window.addEventListener("resize", () => {
  clearTimeout(resizeTimeout);

  resizeTimeout = setTimeout(() => {
    waitForModules(".modules", groupModules);
  }, 1000);
});
