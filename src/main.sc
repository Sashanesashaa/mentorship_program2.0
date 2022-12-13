init: 

   bind("postProcess", function($context){ 

       $context.session.lastState = $context.currentState; 

       log(toPrettyString($context.session)); 

   }); 

require: patterns.sc
require: functions.js
require: discount.yaml
         var = discountInfo
require: dictionary.csv
require: zb-common
         
theme: /

        state: Welcome
            q!: *start
            random:
                a: Привет! Меня зовут {{capitalize($injector.botName)}} Я могу помочь тебе купить билет на самолет. Согласен? 
                a: Привет. Меня зовут {{capitalize($injector.botName)}}  Будем выбирать билеты на самолет? 
            script:
             $jsapi.startSession();
             $response.replies = $response.replies || [];
             $response.replies.push(
                 {
                 "type": "image",
                 "imageUrl": "https://i.pinimg.com/550x/56/85/28/5685285d85774d4c45c402626e9d0c7e.jpg"
                 })
            
            state: Agreement
                q: * (да/давай*/хорошо) *
            if: $client.phone
                go: /NumberWasConfirmedAlready
            else: 
                go: /PhoneAsk
                
            state: Rejection 
                q: (нет/не хочу/ не надо)
                a: А что тогда делать?
        
        state: PhoneAsk
            a: Для продолжения напишите, пожалуйста, мне ваш номер в формате 79000000000. 
            go!: PhoneNumberConfirmation
            
            state: PhoneNumberConfirmation
                q: $phone
                go!: Confirm
            
            state: Confirm
                script: 
                     $temp.phone = $parseTree._phone
                a: Это ваш номер {{$parseTree._phone}}, верно? 
                script:  
                    $session.probablyPhone = $temp.phone
                buttons:
                    "Yes"
                    "No"
                
                state: NumberConfirmed 
                    q: Yes
                    a: ура, подтвердили номер! 
                    
                state: PhoneNumberNonConfirmed
                    q!: No
                    a: Тогда давайте ещё раз. 
                    go!: /PhoneAsk
            
                
        state: NumberWasConfirmedAlready
            q: да
            a: Номер уже подтвержден. 
            script: $client.phone = $session.probablyPhone
            # go!: /info
            
            
        state: CatchAll
            event!: noMatch
            random:
                a: Я не понял, попоробуй сказать по-другому
                a: Ой, что-то на непонятном 
            go!: {{ $session.lastState }}
            
        
        state: PaymentSubs 
            intent!: /оплата_подписки 
            if: $session.showHypotheses 
                script: 
                 log("PAYMENT!!!   " + toPrettyString($context.nBest)); 
                go!: /Hypotheses 
            else: 
                a: Пока в нашем сервисе не реализована возможность платной подписки. 
                script: 
                 $session.showHypotheses = $injector.showHypotheses; 
        
        state: Hypotheses
            script: 
             $reactions.answer("Может быть, вас интересует:");
             $session.showHypotheses = false; 
              // из nBest получаем массив объектов подходящих кнопок 
             var hypothesesButtonsArray = getHypothesesForButtons($context.nBest);    
               // если нашлись какие-то гипотезы с кнопками 
              if (hypothesesButtonsArray.length > 0) { 
                  $reactions.answer("Может быть, вас интересует:"); 
                  _.each(hypothesesButtonsArray, function(hypothesis) { 
                    $reactions.buttons({text: hypothesis.button, transition: hypothesis.path}); 
                     }); 
             } else { 
                $reactions.transition("/Catchall"); 
                } 
             
            
            buttons: 
                "1 option"
                "2 option"
        
        state: Inform
            script:
             var todayDayOfWeek = $jsapi.dateForZone("Europe/Moscow", "EEEE");
             var discount = discountInfo [todayDayOfWeek];
             if (discount) {
              var todayDate = $jsapi.dateForZone("Europe/Moscow", "dd.MM");
              var answerText = "Хочу отметить, что вам крупно повезло! Сегодня (" + nowDate + ") действует акция!"; 
              $reactions.answer(answerText);
              $reactions.anser(discount);
             }
    
# theme: /Discount
#     state: 
#         script: 
#                  var currentDate = $jsapi.dateForZone("Europe/Moscow", "dd.MM");
#                  var answerText = "Хочу отметить, что вам крупно повезло! Сегодня (" + nowDate + ") действует акция!"; 
#                  var discount = "Купите билет сегодня и получите скидку в 10% на следующую покупку!"; 
#                  $reactions.answer(answerText)
#                  $reactions.answer(discount)
            