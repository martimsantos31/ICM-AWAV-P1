import * as functions from 'firebase-functions/v1';
import * as admin from 'firebase-admin';

admin.initializeApp();

// This is the function that gets triggered on new post creation
export const triggerPostNotification = functions.firestore
    .document('feed_posts/{postId}')
    .onCreate(async (snap: functions.firestore.QueryDocumentSnapshot, context: functions.EventContext) => {
        try {
            const post = snap.data();
            if (!post) {
                console.log('No data associated with the event');
                return;
            }

            // Get the post data
            const postId = context.params.postId;
            const authorId = post.authorId;
            const authorName = post.authorName;
            const content = post.content;
            const hasImage = post.imageUrl != null;
            const type = post.type;

            // Don't send notifications for system messages
            if (type === 'ANNOUNCEMENT' || type === 'EVENT_UPDATE') {
                console.log('Skipping notification for system message');
                return;
            }

            // Get all FCM tokens except the author's
            const usersSnapshot = await admin.firestore()
                .collection('users')
                .where('fcmToken', '!=', null)
                .get();

            const tokens: string[] = [];
            usersSnapshot.forEach(doc => {
                const userData = doc.data();
                // Skip the post author
                if (doc.id !== authorId && userData.fcmToken) {
                    tokens.push(userData.fcmToken);
                }
            });

            if (tokens.length === 0) {
                console.log('No tokens to send notifications to');
                return;
            }

            // Prepare notification message
            const message = {
                notification: {
                    title: 'New Post from ' + authorName,
                    body: content.substring(0, 100) + (content.length > 100 ? '...' : ''),
                    image: hasImage ? post.imageUrl : undefined
                },
                data: {
                    postId: postId,
                    type: 'NEW_POST',
                    click_action: 'FLUTTER_NOTIFICATION_CLICK'
                },
                tokens: tokens
            };

            // Send notifications in batches of 500 (FCM limit)
            const batchSize = 500;
            for (let i = 0; i < tokens.length; i += batchSize) {
                const batch = tokens.slice(i, i + batchSize);
                const batchMessage = {
                    ...message,
                    tokens: batch
                };

                try {
                    const response = await admin.messaging().sendMulticast(batchMessage);
                    console.log('Successfully sent messages:', response);
                    console.log('Success count:', response.successCount);
                    console.log('Failure count:', response.failureCount);
                } catch (error) {
                    console.error('Error sending batch:', error);
                }
            }

            console.log('Notification process completed');
        } catch (error) {
            console.error('Error in triggerPostNotification:', error);
        }
    });

// This is the callable function that can be invoked directly from the app
export const triggerPostNotificationCallable = functions.https.onCall(async (data, context) => {
    try {
        // For testing, allow unauthenticated access
        // In production, you should remove this and use proper authentication
        /*
        // Verify authentication
        if (!context.auth) {
            throw new functions.https.HttpsError(
                'unauthenticated', 
                'You must be logged in to use this feature.'
            );
        }
        */

        // Get post data from the request
        const postId = data.postId;
        const authorId = data.authorId || (context.auth?.uid || 'anonymous');
        const authorName = data.authorName;
        const content = data.content;
        const hasImage = data.hasImage === 'true' || data.hasImage === true;
        const type = data.type;

        // Don't send notifications for system messages
        if (type === 'ANNOUNCEMENT' || type === 'EVENT_UPDATE') {
            console.log('Skipping notification for system message');
            return { success: true, message: 'Skipped notification for system message' };
        }

        // Get all FCM tokens except the author's
        const usersSnapshot = await admin.firestore()
            .collection('users')
            .where('fcmToken', '!=', null)
            .get();

        const tokens: string[] = [];
        usersSnapshot.forEach(doc => {
            const userData = doc.data();
            // Skip the post author
            if (doc.id !== authorId && userData.fcmToken) {
                tokens.push(userData.fcmToken);
            }
        });

        if (tokens.length === 0) {
            console.log('No tokens to send notifications to');
            return { success: true, message: 'No recipients found' };
        }

        // Prepare notification message
        const message = {
            notification: {
                title: 'New Post from ' + authorName,
                body: content.substring(0, 100) + (content.length > 100 ? '...' : ''),
                image: hasImage ? data.imageUrl : undefined
            },
            data: {
                postId: postId,
                type: 'NEW_POST',
                click_action: 'FLUTTER_NOTIFICATION_CLICK'
            },
            tokens: tokens
        };

        // Send notifications in batches of 500 (FCM limit)
        const batchSize = 500;
        let successCount = 0;
        let failureCount = 0;

        for (let i = 0; i < tokens.length; i += batchSize) {
            const batch = tokens.slice(i, i + batchSize);
            const batchMessage = {
                ...message,
                tokens: batch
            };

            try {
                const response = await admin.messaging().sendMulticast(batchMessage);
                console.log('Successfully sent messages:', response);
                successCount += response.successCount;
                failureCount += response.failureCount;
            } catch (error) {
                console.error('Error sending batch:', error);
                failureCount += batch.length;
            }
        }

        console.log('Notification process completed');
        return { 
            success: true, 
            message: 'Notifications sent',
            stats: {
                successCount,
                failureCount,
                totalRecipients: tokens.length
            }
        };
    } catch (error) {
        console.error('Error in triggerPostNotificationCallable:', error);
        throw new functions.https.HttpsError('internal', 'Error sending notifications');
    }
}); 