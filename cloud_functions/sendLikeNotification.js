'use-strict'

const functions = require('firebase-functions');
const admin     = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendLikeNotification = functions.firestore.document("Posts/{post_id}/Liked_Users/{user_id}").onWrite((change,context)=> {

    const post_id = context.params.post_id;
    const user_id = context.params.user_id;
    const db      = admin.firestore();
  
    return db.collection("Posts").doc(post_id).get().then((queryResult)=>{

        const admin_id = queryResult.data().userId;
      
        return db.collection("Posts").doc(post_id).collection("Liked_Users").doc(user_id).get().then((queryRes)=>{
       
        const liked = queryRes.data().liked;
        const admin_data = db.collection("Users").doc(admin_id).get();
        const liker_data = db.collection("Users").doc(user_id).get();
        let tokens;  // The array containing all the user's tokens.

            return Promise.all([liker_data, admin_data]).then(result=>{

                const liker_name   = result[0].data().name;
                const liker_image  = result[0].data().image;
                const admin_name   = result[1].data().name;
                const admin_tokens = result[1].data().token_ids;

                tokens = Object.keys(admin_tokens).map(function(key) { // Listing all tokens as an array.
                    return admin_tokens[key];
                });

                if (liker_name != admin_name) {
                    const payload = {
                        data:{
                            post_id:post_id,
                            admin_id:admin_id,
                            title:liker_name,
                            from_image:liker_image,
                            body:"Liked your post",
							notification_type:"like",
							channel_name:"FrenzApp Posts",
                            click_action:"com.nsromapa.frenzapp.TARGET_LIKE"
                        } 
                    };
                    
                    console.log(" | to: " + admin_name + " | from:" + liker_name + " | message:" + liker_name + " has liked your post.");
                    // Send notifications to all tokens.
                  if(liked){
                    return admin.messaging().sendToDevice(tokens, payload);  
                  }
                }         
                    }).then((response) => {
                        // For each message check if there was an error.
                        let tokensToRemove = [];
                        response.results.forEach((result, index) => {
                        const error = result.error;
                            if (error) {
                                console.error('Failure sending like notification to', tokens[index], error);
                                // Cleanup the tokens who are not registered anymore.
                                if (error.code === 'messaging/invalid-registration-token' ||
                                    error.code === 'messaging/registration-token-not-registered') {
                                    tokensToRemove.push(          toRef.update({token_ids: db.FieldValue.arrayRemove(tokens[index])})           );
                                }
                            }
                            else {
                                console.log("Successfully sent like notification: ", tokens[index], response);
                            }
                        });
                        return Promise.all(tokensToRemove);
             });   
        });             
         
    });
          
});