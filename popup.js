document.addEventListener('DOMContentLoaded', () => {
  chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
    chrome.scripting.executeScript(
      {
        target: { tabId: tabs[0].id },
        function: fetchAndDisplayJSON
      }
    );
  });
});

function fetchAndDisplayJSON() {
  try {
    const jsonContent = JSON.parse(document.body.innerText);
    document.body.innerHTML = `<pre>${JSON.stringify(jsonContent, null, 2)}</pre>`;
  } catch (e) {
    document.body.innerText = "This is not a valid JSON content.";
  }
}
