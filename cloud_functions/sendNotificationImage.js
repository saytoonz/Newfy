'use-strict'

const functions = require('firebase-functions');
const admin     = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotificationImage=functions.firestore.document("Users/{user_id}/Notifications_image/{notification_id}").onWrite((change,context)=> {

    const user_id         = context.params.user_id;
    const notification_id = context.params.notification_id;
    const db              = admin.firestore();

    return db.collection("Users").doc(user_id).collection("Notifications_image").doc(notification_id).get().then(queryResult=>{

        const from_user_id    = queryResult.data().from;
        const from_message    = queryResult.data().message;
        const image           = queryResult.data().image;
        const notification_id = queryResult.data().notification_id;
        const timestamp       = queryResult.data().timestamp;

        const from_data = db.collection("Users").doc(from_user_id).get();
        const to_data   = db.collection("Users").doc(user_id).get();
        let tokens;  // The array containing all the user's tokens.

        return Promise.all([from_data, to_data]).then(result=>{

            const from_name  = result[0].data().name;
            const from_image = result[0].data().image;
            const to_name    = result[1].data().name;
            const to_tokens  = result[1].data().token_ids;
            
            tokens = Object.keys(to_tokens).map(function(key) { 
                return to_tokens[key]; // Listing all tokens as an array.
              });

            const payload={
                data:{
                    notification_id:notification_id,
                    timestamp:timestamp,
                    message:from_message,
                    from_id:from_user_id,
                    from_name:from_name,
                    from_image:from_image,
                    image:image,
                    title:from_name,
					notification_type:"Message",
					channel_name:"Flash Messages",
                    body:"Sent you a image with message '" + from_message + "'",
                    click_action:"com.nsromapa.frenzapp.TARGETNOTIFICATION_IMAGE"
                }
            };
                console.log(" | from: " + from_name + " | to:" + to_name + " | message:" + from_message + " | Sent an image");
               // Send notifications to all tokens.
               return admin.messaging().sendToDevice(tokens, payload);
            }).then((response) => {
                // For each message check if there was an error.
                let tokensToRemove = [];
                response.results.forEach((result, index) => {
                const error = result.error;
                    if (error) {
                        console.error('Failure sending notification to', tokens[index], error);
                        // Cleanup the tokens who are not registered anymore.
                        if (error.code === 'messaging/invalid-registration-token' ||
                            error.code === 'messaging/registration-token-not-registered') {
                            tokensToRemove.push(          toRef.update({token_ids: db.FieldValue.arrayRemove(tokens[index])})           );
                        }
                    }
                    else {
                        console.log("Successfully sent notification to: ", tokens[index], response);
                    }
                });
                return Promise.all(tokensToRemove);
            });             
         });
    });