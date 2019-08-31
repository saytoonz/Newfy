'use-strict'

const functions = require('firebase-functions');
const admin     = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

let toRef;


exports.sendAcceptFriendRequest = functions.firestore.document("Notifications/{user_id}/Accepted_Friend_Requests/{user_email}").onWrite((change,context)=> {

    const user_id  = context.params.user_id;
    const email_id = context.params.user_email;
    const db       = admin.firestore();
  
    console.log("id: " + user_id + " email: " + email_id);
  
    toRef   = db.collection('Users').doc(user_id);
    
    return db.collection("Notifications").doc(user_id).collection("Accepted_Friend_Requests").doc(email_id).get().then((queryResult)=>{

            const from_user_id = queryResult.data().id;
            const from_name    = queryResult.data().name;
            const from_email   = queryResult.data().email;
            const from_imagee  = queryResult.data().image;
            const noti_id      = queryResult.data().notification_id;
            const timestamp    = queryResult.data().timestamp;
        
            const from_data = db.collection("Users").doc(from_user_id).get();
            const to_data   = db.collection("Users").doc(user_id).get();

            let tokens; // The array containing all the user's tokens.

            // https://howtofirebase.com/promises-for-firebase-bbb9d0d595ed
            return Promise.all([from_data, to_data]).then(result=>{

                const from_image = result[0].data().image;
                const from_email = result[0].data().email;
                
                const to_name    = result[1].data().name;
                const to_email   = result[1].data().email;
                const to_image   = result[1].data().image;
                const to_id      = result[1].data().id;
                const to_tokens  = result[1].data().token_ids;
                
                tokens = Object.keys(to_tokens).map(function(key) {
                return to_tokens[key];
                });
            
            const payload={
                    data:{
                        notification_id:noti_id,
                        timestamp:timestamp,
                        friend_id:from_user_id,
                        friend_name:from_name,
                        friend_email:from_email,
                        friend_image:from_imagee, // friend_token:from_tokenn string gone
                        title:from_name,
						notification_type:"Friend request",
						channel_name:"Friend Requests",
                        body:"Accepted your friend request",
                        click_action:"com.nsromapa.frenzapp.TARGET_ACCEPTED"
                    }   
                };

                console.log(" | to: " + from_name + " | from:" + to_name+" | message:" + from_name + " accepted your friend request.");

                // Send notifications to all tokens.
                return admin.messaging().sendToDevice(tokens, payload);

            }).then((response) => {
                // For each message check if there was an error.
                let tokensToRemove = [];
                response.results.forEach((result, index) => {
                const error = result.error;
                    if (error) {
                        console.error('Failure sending acceptance notification to', tokens[index], error);
                        // Cleanup the tokens who are not registered anymore.
                        if (error.code === 'messaging/invalid-registration-token' ||
                            error.code === 'messaging/registration-token-not-registered') {
                            tokensToRemove.push(          toRef.update({token_ids: db.FieldValue.arrayRemove(tokens[index])})           );
                        }
                    }
                    else {
                        console.log("Successfully accepted friend request: ", tokens[index], response);
                    }
                });
                return Promise.all(tokensToRemove);
            });             
        });
});