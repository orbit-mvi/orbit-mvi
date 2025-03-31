const fs = require("fs");
const path = require("path");

async function fetchRemoteJSON() {
  const response = await fetch("https://api.github.com/repos/orbit-mvi/orbit-mvi/releases/latest");
  return response.json();
}

module.exports = function remoteJSONPlugin(context, options) {
  return {
    name: "github-latest-release",

    async loadContent() {
      // Fetch the remote JSON
      const jsonData = await fetchRemoteJSON();

      // Save JSON data to a file inside `generated`
      const jsonFilePath = path.join(__dirname, "generated", "data.json");
      fs.mkdirSync(path.dirname(jsonFilePath), { recursive: true });
      fs.writeFileSync(jsonFilePath, JSON.stringify(jsonData, null, 2));
    },
  };
};
