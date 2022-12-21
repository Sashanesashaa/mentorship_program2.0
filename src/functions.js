function getHypothesesForButtons(nBest) { 

    var hypothesesButtonsArray = []; 

    _.each(nBest, function(hypothesis) { 

        // clazz - путь 

        // debugInfo.intent.answer - названия кнопки (поле "ответ" в интерфейте кайлы) 

        if (hypothesis && hypothesis.clazz && hypothesis.debugInfo && hypothesis.debugInfo.intent && hypothesis.debugInfo.intent.answer) { 

            var hypothesesButtons = {}; 

            hypothesesButtons.button = hypothesis.debugInfo.intent.answer; 

            hypothesesButtons.path = hypothesis.clazz; 

            hypothesesButtons.score = hypothesis.score; 

            hypothesesButtonsArray.push(hypothesesButtons); 

        } 

    }); 

    return hypothesesButtonsArray; 

} 

function getCurrentWeather (lat, lon) {
    
    var apiKey = $jsapi.context().injector.weatherApiKey; 
    
    var response = $http.get("http://api.openweathermap.org/data/2.5/weather?APPID=${APPID}&units=${units}&lang=${lang}&lat=${lat}&lon=${lon}", { 

            timeout: 10000, 
            query:{ 
                APPID: apiKey, 
                units: "metric", 
                lang: "ru", 
                lat: lat, 
                lon: lon
            } 
        
    }); 
    if (!response.isOk || !response.data) { 
        return false; 
     } 
    var weather = {}; 
    weather.temp = response.data.main.temp; 
    weather.description = response.data.weather[0].description; 
    return weather; 
    
} 
