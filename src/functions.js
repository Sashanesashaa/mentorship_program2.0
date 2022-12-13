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