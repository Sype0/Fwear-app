const FDROID_REPO_URL = "https://f-droid.org/repo/index-v1.json";
let cachedIndex = null;
let lastFetchTime = 0;

export default async function handler(request, response) {
  const { q } = request.query;

  if (!q) {
    return response.status(400).json({ 
      error: "Eksik parametre. Lütfen ?q=uygulama_adi şeklinde arama yapın." 
    });
  }

  try {
    const currentTime = Date.now();
    
    if (!cachedIndex || (currentTime - lastFetchTime > 3600 * 1000)) {
      console.log("📥 F-Droid veritabanı indiriliyor...");
      
      const fetchResponse = await fetch(FDROID_REPO_URL);
      if (!fetchResponse.ok) throw new Error("F-Droid sunucusuna erişilemedi.");
      
      const data = await fetchResponse.json();
      
      cachedIndex = data.apps.map(app => ({
        n: app.name,                
        p: app.packageName,        
        i: app.icon,             
        s: app.summary,               
        v: app.suggestedVersionCode
      }));
      
      lastFetchTime = currentTime;
      console.log(`Veritabanı güncellendi. Toplam Uygulama: ${cachedIndex.length}`);
    }
   
    const searchLower = q.toLowerCase();
    
   
    const results = cachedIndex.filter(app => {
      const nameMatch = app.n && app.n.toLowerCase().includes(searchLower);
      const packageMatch = app.p && app.p.toLowerCase().includes(searchLower);
    
      return nameMatch || packageMatch;
    });
    const limitedResults = results.slice(0, 20);

    const formattedResponse = limitedResults.map(app => ({
      name: app.n,
      id: app.p,
      icon: `https://f-droid.org/repo/${app.i}`,
      summary: app.s || "Açıklama yok",
      apkUrl: `https://f-droid.org/repo/${app.p}_${app.v}.apk`
    }));

    response.setHeader('Cache-Control', 's-maxage=3600, stale-while-revalidate');
    response.status(200).json(formattedResponse);

  } catch (error) {
    console.error("Hata:", error);
    response.status(500).json({ error: "Sunucu hatası oluştu." });
  }
}