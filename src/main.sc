require: patterns.sc
theme: /

        state: Welcome
            q!: *start
            random:
                a: Привет! Я могу помочь тебе купить билет на самолет. Согласен? 
                a: Привет. Будем выбирать билеты на самолет? 
            script: 
             $response.replies = $response.replies || [];
             $response.replies.push(
                 {
                 "type": "image",
                 "imageUrl": "https://i.pinimg.com/550x/56/85/28/5685285d85774d4c45c402626e9d0c7e.jpg"
                 })
            
            state: Agreement
                q: (да/давай/ок/хорошо)
                a: Куда полетим? 
                
            state: Rejection 
                q: (нет/не хочу/ не надо)
                a: А что тогда делать?
        
        state: RecievePhoneNumber
            q!: $phone
            script: 
             $temp.phone = $parseTree._phone
             $session.probablyPhone = $temp.phone
            a: {{$parseTree._phone}}
    
    # state: Number
    #      q: $phone 
    #      script: 
    #          $temp.phone = $parseTree._phone
            
            
        state: CatchAll
            event!: noMatch
            random:
                a: Я не понял, попоробуй сказать по-другому
                a: Ой, что-то на непонятном 
