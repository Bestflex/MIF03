TP7 WEB

Partie 1 :

var startTiming = window.performance.timeOrigin;
var htmlLoadTime = window.performance.timing.domInteractive - startTiming;
console.log("Temps de récupération de la page HTML : " + htmlLoadTime + " ms");

function averageResponseTime(data) {
  data.sort(function(a, b) { return a.responseEnd - b.responseEnd; });
  var trimmedData = data.slice(2, data.length - 2);
  var total = trimmedData.reduce(function(acc, item) { return acc + item.responseEnd; }, 0);
  return total / trimmedData.length;
}

function printResults(category, filterFn) {
  var results = [];
  for (var i = 0; i < 10; i++) {
    var entries = window.performance.getEntriesByType('resource').filter(filterFn);
    if (entries.length > 0) {
      results.push(averageResponseTime(entries));
    }
  }
  var avgTime = results.reduce(function(acc, time) { return acc + time; }, 0) / results.length;
  console.log("Average loading time for " + category + ": " + avgTime + " ms");
}

// Mesurer le temps d'affichage de l'app shell
printResults("App Shell", function(entry) {
  return entry.entryType === 'resource' &&
         (entry.initiatorType === 'script' || entry.initiatorType === 'link');
});

// Mesurer le temps d'affichage du chemin critique de rendu (CRP)
printResults("Critical Rendering Path", function(entry) {
  return true; // Toutes les entrées pour le CRP
});

// Ajouter la mesure du temps de chargement de la page HTML initiale
measureAndLog("HTML Load", function(entry) {
  return entry.entryType === 'resource' && entry.initiatorType === 'document';
});

function measureAndLog(category, filterFn) {
  var entries = window.performance.getEntriesByType('resource').filter(filterFn);
  if (entries.length > 0) {
    var avgTime = averageResponseTime(entries);
    console.log("Average loading time for " + category + ": " + avgTime + " ms");
  }
}

"déploiement sur Tomcat"

Temps de récupération de la page HTML : 461.10009765625 ms
Average loading time for App Shell: 294.92500000447035 ms
Average loading time for Critical Rendering Path: 444886.8264227699 ms

Partie 2 :

var startTiming = window.performance.timeOrigin;
var htmlLoadTime = window.performance.timing.domInteractive - startTiming;

console.log("Temps de récupération de la page HTML : " + htmlLoadTime + " ms");

function averageResponseTime(data) {
  data.sort(function(a, b) { return a.responseEnd - b.responseEnd; });
  var trimmedData = data.slice(2, data.length - 2);
  var total = trimmedData.reduce(function(acc, item) { return acc + item.responseEnd; }, 0);
  return total / trimmedData.length;
}

function printResults(category, filterFn) {
  var results = [];
  for (var i = 0; i < 10; i++) {
    var entries = window.performance.getEntriesByType('resource').filter(filterFn);
    if (entries.length > 0) {
      results.push(averageResponseTime(entries));
    }
  }
  var avgTime = results.reduce(function(acc, time) { return acc + time; }, 0) / results.length;
  console.log("Average loading time for " + category + ": " + avgTime + " ms");
}

// Mesurer le temps d'affichage de l'app shell
printResults("App Shell", function(entry) {
  return entry.entryType === 'resource' &&
         (entry.initiatorType === 'script' || entry.initiatorType === 'link');
});

// Mesurer le temps d'affichage du chemin critique de rendu (CRP)
printResults("Critical Rendering Path", function(entry) {
  return true; // Toutes les entrées pour le CRP
});

// Ajouter la mesure du temps de chargement de la page HTML initiale
measureAndLog("HTML Load", function(entry) {
  return entry.entryType === 'resource' && entry.initiatorType === 'document';
});

function measureAndLog(category, filterFn) {
  var entries = window.performance.getEntriesByType('resource').filter(filterFn);
  if (entries.length > 0) {
    var avgTime = averageResponseTime(entries);
    console.log("Average loading time for " + category + ": " + avgTime + " ms");
  }
}


"déploiement sur nginx"


Temps de récupération de la page HTML : 377.10009765625 ms
Average loading time for App Shell: 256.2749999910593 ms
Average loading time for Critical Rendering Path: 60404.02058822823 ms


"Amélioration"


le temps de récupération de la page HTML a connu une amélioration d'environ 18.25%.
Le temps moyen de chargement de l'App Shell a connu une amélioration d'environ 13.13%
Le temps moyen de chargement du chemin critique de rendu a connu une amélioration significative d'environ 86.41%.

Partie 3 :

"déploiement sur Tomcat"

"Utilisation d'attributs async et/ou defer pour décaler le chargement de scripts non nécessaires au CRP "

Temps de récupération de la page HTML : 217.699951171875 ms
Average loading time for App Shell: 352.54999999701977 ms
Average loading time for Critical Rendering Path: 32229.7431034415 ms

"Amélioration"


le temps de récupération de la page HTML a connu une amélioration d'environ 52.81%.
Le temps moyen de chargement de l'App Shell a connu une amélioration d'environ -19.52%
Le temps moyen de chargement du chemin critique de rendu a connu une amélioration significative d'environ 92.76%.

"Description"

on a ajouté les attributs async et defer à nos scripts. Cela permet de rendre le téléchargement des scripts asynchrone (async), 
ce qui ne bloque pas le rendu de la page, et de différer leur exécution jusqu'à ce que le document HTML soit entièrement analysé (defer).

"Utilisation des CDN"

"Les CDN permettent une gestion efficace de la charge, une distribution rapide des fichiers, 
l'optimisation des images, le caching, et offrent des fonctionnalités de sécurité, 
contribuant ainsi à une expérience utilisateur plus rapide, fiable et sécurisée."

"Ajout delai chargement image"

A la suite de l'ajout des CDN on est passé a 100% de performance donc on a preferé ne pas realiser les autres optimisation 



