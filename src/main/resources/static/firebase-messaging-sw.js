// firebase-messaging-sw.js
importScripts("https://www.gstatic.com/firebasejs/9.6.1/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/9.6.1/firebase-messaging-compat.js");

// 애플리케이션 정보 직접 입력해서 쓰세요
firebase.initializeApp({
  apiKey: "",
  authDomain: "",
  projectId: "",
  storageBucket: "",
  messagingSenderId: "",
  appId: "",
  measurementId: ""
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage(function(payload) {
  console.log("백그라운드 메시지 수신됨:", payload);
  const { title, body } = payload.data;
  self.registration.showNotification(title, {
    body: body
  });
});
