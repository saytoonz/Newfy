'use-strict'

const functions = require('firebase-functions');
const admin     = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendCommentNotification=functions.firestore.document("Notifications/{user_id}/Comment/{comment_id}").onWrite((change,context)=> {

    const user_id    = context.params.user_id;
    const comment_id = context.params.comment_id;
    const db         = admin.firestore();
  
    console.log(user_id + ":" + comment_id);

    return db.collection("Notifications").doc(user_id).collection("Comment").doc(comment_id).get().then((queryResult)=>{

            const post_id       = queryResult.data().post_id;
            const admin_user_id = queryResult.data().admin_id;
            const noti_id       = queryResult.data().notification_id;
            const timestamp     = queryResult.data().timestamp;    
            const post_desc     = queryResult.data().post_desc;

            const admin_data     = db.collection("Users").doc(admin_user_id).get();
            const commenter_data = db.collection("Users").doc(user_id).get();
            let tokens;  // The array containing all the user's tokens.

            return Promise.all([commenter_data, admin_data]).then(result=>{

                const commenter_name  = result[0].data().name;
                const commenter_image = result[0].data().image;
                const admin_name      = result[1].data().name;
                const admin_tokens    = result[1].data().token_ids;
                    
                tokens = Object.keys(admin_tokens).map(function(key) {
                    return admin_tokens[key];
                });

            if (commenter_name != admin_name) {
                const payload={
                    data:{
                        notification_id:noti_id,
                        timestamp:timestamp,
                        post_id:post_id,
                        admin_id:admin_user_id,
                        title:commenter_name,
                        from_image:commenter_image,
                        post_desc:post_desc,
						notification_type:"comment",
						channel_name:"FrenzApp Posts",
                        body:"Commented on your post",
                        click_action:"com.nsromapa.frenzapp.TARGET_COMMENT"
                        }
                    };

                console.log(" | to: " + admin_name + " | from:" + commenter_name + " | message:" + commenter_name + " has left a comment.");
                return admin.messaging().sendToDevice(tokens, payload);   
                }

            }).then((response) => {
                // For each message check if there was an error.
                let tokensToRemove = [];
                response.results.forEach((result, index) => {
                const error = result.error;
                if (error) {
                    console.error('Failure sending comment notification to', tokens[index], error);
                    // Cleanup the tokens who are not registered anymore.
                    if (error.code === 'messaging/invalid-registration-token' ||
                        error.code === 'messaging/registration-token-not-registered') {
                        tokensToRemove.push(          toRef.update({token_ids: db.FieldValue.arrayRemove(tokens[index])})           );
                    }
                }
                else {
                    console.log("Successfully sent comment: ", tokens[index], response);
                }
            });
            return Promise.all(tokensToRemove);
        });             
    });
});