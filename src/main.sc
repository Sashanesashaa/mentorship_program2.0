init: 

   bind("postProcess", function($context){ 

       $context.session.lastState = $context.currentState; 

       log(toPrettyString($context.session)); 

   }); 

require: patterns.sc
require: functions.js
require: discount.yaml
         var = discountInfo
# require: dictionary.csv

require: slotfilling/slotFilling.sc
    module = sys.zb-common
    
require: city/cities-ru.csv 
   module = sys.zb-common 
   name = Cities 
   var = $Cities 
   
require: functions.js

patterns: 
    # $City = $entity<Cities> || converter = function(parseTree) {var id = parseTree.City[0].Cities[0].value; return $Cities[id].value;};
    $City = $entity<Cities> || converter = function(parseTree) {var id = parseTree.Cities[0].value; return $Cities[id].value;}; 
    # $City = $entity<Cities> || converter = function(parseTree) {var id = parseTree._Cities; return $Cities[id].value;};
         
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
            go!: /Discount/Info
            
            
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
        
        # state: Inform
        #     script:
        #      var todayDayOfWeek = $jsapi.dateForZone("Europe/Moscow", "EEEE");
        #      var discount = discountInfo [todayDayOfWeek];
        #      if (discount) {
        #       var todayDate = $jsapi.dateForZone("Europe/Moscow", "dd.MM");
        #       var answerText = "Хочу отметить, что вам крупно повезло! Сегодня (" + nowDate + ") действует акция!"; 
        #       $reactions.answer(answerText);
        #       $reactions.anser(discount);
        #      }
    
theme: /Discount
    state: Info
        q!: инфо
        script: 
             var todayDayOfWeek = $jsapi.dateForZone("Europe/Moscow", "EEEE");
             var discount = discountInfo [todayDayOfWeek];
             if (discount) {
              var todayDate = $jsapi.dateForZone("Europe/Moscow", "dd.MM");
              var answerText = "Хочу отметить, что вам крупно повезло! Сегодня (" + todayDayOfWeek + ") действует акция!"; 
              $reactions.answer(answerText);
              $reactions.answer(discount);
             }
        go!: /City/Departure

theme: /City
    
    state: Departure
        a: Из какого города отчаливаете?
        
        state: GetCity
            q: * $City * 
            script: 
             $session.departureCity = $parseTree._City; 
            #   log("PARSE TREE" + toPrettyString($parseTree))
            a: Итак, город отправления: {{ $session.departureCity.name }}. 
            go!: ./Arrival
            
            state: Arrival 
                a: Назовите город прибытия 
                state: Get
                    q: * $City * 
                    script: 
                     $session.arrivalCity = $parseTree._City; 
                    a: Итак, город прибытия: {{ $session.arrivalCity.name }}. 
                    go!:  /Weather/Current
                    
    
    state: Direction
        intent!: /direction
        # a: Итак, вы хотите купить билет! {{$parseTree}}
        script: 
          log("PARSE TREE" + toPrettyString($parseTree));
          $session.departureCity = $nlp.inflect($parseTree._departure, "nomn") || $parseTree._departure; 
          $session.arrivalCity = $nlp.inflect($parseTree._destination, "nomn") || $parseTree._destination; 
          $session.departureCity = capitalize($session.departureCity); 
          $session.arrivalCity = capitalize($session.arrivalCity); 
          $session.date = $parseTree._date.year + "/" + $parseTree._date.month + "/" + $parseTree._date.day; 
          delete $session.arrivalCoordinates; 
        a: {{ $session.date }} отправляемся из города {{ $session.departureCity }} в город {{ $session.arrivalCity }}.
        
    state: LocalCatchAll
        q: * || fromState = /City 
        a: Простите, я вас не поняла. 
        go!: {{ $session.lastState }} 
        
theme: /Weather 
    state: Current
        script: 
         $temp.currentWeather = getCurrentWeather($session.arrivalCoordinates.lat, $session.arrivalCoordinates.lon); 
        if: $temp.currentWeather 
        a: В городе {{ $session.arrivalCity }} сейчас {{ $temp.currentWeather.description }} {{ $temp.currentWeather.temp }}°C. 