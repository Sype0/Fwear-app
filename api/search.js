const FDROID_REPO_URL = "https://f-droid.org/repo/index-v1.json";

let cachedApps = null;
let lastFetchTime = 0;

export default async function handler(request, response) {
  const { q } = request.query;

  if (!q) {
    return response.status(400).json({ error: "Missing query parameter" });
  }

  try {
    const now = Date.now();

    if (!cachedApps || (now - lastFetchTime > 3600 * 1000)) {
      const fetchResponse = await fetch(FDROID_REPO_URL);
      const data = await fetchResponse.json();
      cachedApps = data.apps || [];
      lastFetchTime = now;
    }

    const searchLower = q.toLowerCase();

    let results = cachedApps.filter(app => {
      const name = (app.name || "").toLowerCase();
      const pkg = (app.packageName || "").toLowerCase();
      const summary = (app.summary || "").toLowerCase();

      return name.includes(searchLower) || 
             pkg.includes(searchLower) || 
             summary.includes(searchLower);
    });

    results.sort((a, b) => {
      const nameA = (a.name || "").toLowerCase();
      const nameB = (b.name || "").toLowerCase();
      
      const startsA = nameA.startsWith(searchLower);
      const startsB = nameB.startsWith(searchLower);

      if (startsA && !startsB) return -1;
      if (!startsA && startsB) return 1;
      return 0;
    });

    const cleanResults = results.slice(0, 20).map(app => ({
      name: app.name,
      id: app.packageName,
      icon: `https://f-droid.org/repo/${app.icon}`,
      summary: app.summary || "",
      version: app.suggestedVersionName || "Latest",
      apkUrl: `https://f-droid.org/repo/${app.packageName}_${app.suggestedVersionCode}.apk`
    }));

    response.setHeader('Cache-Control', 's-maxage=3600, stale-while-revalidate');
    return response.status(200).json(cleanResults);

  } catch (error) {
    return response.status(500).json({ error: "Internal Server Error" });
  }
}