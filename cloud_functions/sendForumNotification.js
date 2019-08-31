'use-strict'

const functions = require('firebase-functions');
const admin     = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendForumNotification=functions.firestore.document("Questions/{question_id}/Answers/{answer_id}").onWrite((change,context)=> {

    const question_id = context.params.question_id;
    const answer_id   = context.params.answer_id;
    const db          = admin.firestore();
  
    console.log(question_id + ":" + answer_id);

    return db.collection("Questions").doc(question_id).get().then((questionqueryResult)=>{

        const question_owner_id  = questionqueryResult.data().id;
        const question           = questionqueryResult.data().question;
        const question_timestamp = questionqueryResult.data().timestamp;

        return db.collection("Questions").doc(question_id).collection("Answers").doc(answer_id).get().then((answerqueryResult)=>{
        
            const answered_user_id = answerqueryResult.data().user_id;
            const answer           = answerqueryResult.data().answer;
            const is_answer        = answerqueryResult.data().is_answer;
            const timestamp        = answerqueryResult.data().timestamp;

            const question_owner_data = db.collection("Users").doc(question_owner_id).get();
            const answer_owner_data   = db.collection("Users").doc(answered_user_id).get();
            let question_tokens, answer_tokens;  // The array containing all the user's tokens.

            return Promise.all([question_owner_data, answer_owner_data]).then(result=>{

                const question_poster_name  = result[0].data().name;
                const question_poster_token = result[0].data().token_ids;
                const question_poster_image = result[0].data().image;
                const answer_poster_name    = result[1].data().name;
                const answer_poster_token   = result[1].data().token_ids;
                const answer_poster_image   = result[1].data().image;
            
                question_tokens = Object.keys(question_poster_tokens).map(function(key) {
                    return question_poster_tokens[key];
                });

                answer_tokens = Object.keys(answer_poster_tokens).map(function(key) {
                    return answer_poster_tokens[key];
                });

            if (answer_poster_token != question_poster_token)
            {
                if (is_answer == "yes") {
                    
                    const payload={
                    data:{
                        question_timestamp:question_timestamp,
                        channel:"Forum",
                        timestamp:timestamp,
                        question_id:question_id,
                        title:"Forum",
						notification_type:"forum",
						channel_name:"FrenzApp Forum",
                        from_image:question_poster_image,
                        body:question_poster_name + " marked your answer correct for the question \"" + question + "\"",
                        click_action:"com.nsromapa.frenzapp.TARGET_FORUM"
                        }
                    };
                
                    return admin.messaging().sendToDevice(answer_tokens, payload).then(response=>{

                        // For each message check if there was an error.
                        let tokensToRemove = [];
                        response.results.forEach((result, index) => {
                        const error = result.error;
                            if (error) {
                                console.error('Failure sending Marked As Answer Forum notification to', answer_tokens[index], error);
                                // Cleanup the tokens who are not registered anymore.
                                if (error.code === 'messaging/invalid-registration-token' ||
                                    error.code === 'messaging/registration-token-not-registered') {
                                tokensToRemove.push(      toRef.update({token_ids: db.FieldValue.arrayRemove(answer_tokens[index])})      );
                                }
                            }
                            else {
                                console.log("Successfully sent Marked As Answer Forum notification: ", answer_tokens[index], response);
                            }
                        });
                    });
                }
                else { 
                        const payload={
                            data:{
                            question_timestamp:question_timestamp,
                            channel:"Forum",
                            timestamp:timestamp,
                            question_id:question_id,
                            title:"Forum",
							notification_type:"forum",
							channel_name:"FrenzApp Forum",
                            from_image:answer_poster_image,
                            body:answer_poster_name + " answered to your question \"" + question + "\"",
                            click_action:"com.nsromapa.frenzapp.TARGET_FORUM"
                            }
                        };

                        return admin.messaging().sendToDevice(question_tokens, payload).then(response=>{
                            // For each message check if there was an error.
                            let tokensToRemove = [];
                            response.results.forEach((result, index) => {
                            const error = result.error;
                                if (error) {
                                    console.error('Failure sending Marked As Answer Forum notification to', question_tokens[index], error);
                                    // Cleanup the tokens who are not registered anymore.
                                    if (error.code === 'messaging/invalid-registration-token' ||
                                        error.code === 'messaging/registration-token-not-registered') {
                                    tokensToRemove.push(     toRef.update({token_ids: db.FieldValue.arrayRemove(question_tokens[index])})     );
                                    }
                                }
                                else {
                                    console.log("Successfully sent Answer Forum notification: ", question_tokens[index], response);
                                }
                            });
                        });
                    }
                }        
            });
        });
    });
});