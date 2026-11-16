const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const serverNow = () => new Date(Date.now() - 2000);

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validPrefsPayload(uid) {
  const ts = serverNow();
  return {
    userId: uid,
    appLanguage: "ENGLISH",
    voiceLanguage: "BENGALI",
    durationMinutes: 15,
    autoTurnOff: true,
    autoCallResponseEnabled: false,
    calculationMethod: "KARACHI",
    asrSchool: "HANAFI",
    touchSoundsEnabled: true,
    hapticFeedbackEnabled: true,
    soundVolume: 0.75,
    hapticIntensity: "MEDIUM",
    tasbihZikrId: "subhanallah",
    tasbihDailyCount: 33,
    tasbihLifetimeCount: 120,
    tasbihDailyGoal: 100,
    tasbihDateKey: "2026-11-16",
    duaBookmarks: ["dua_morning_1"],
    notifEnabled: true,
    notifMinutesBefore: 5,
    notifPlaySound: true,
    createdAt: ts,
    updatedAt: ts,
  };
}

test("Unauthenticated user cannot read or write user preferences", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validPrefsPayload(ALICE_UID)));
});

test("Authenticated user can create, read, and update own preferences", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(docRef.set(validPrefsPayload(ALICE_UID)));
  await assertSucceeds(docRef.get());
  await assertSucceeds(
    docRef.update({
      appLanguage: "BENGALI",
      tasbihDailyCount: 34,
      updatedAt: serverNow(),
    })
  );
});

test("Cross-user access to preferences fails with PERMISSION_DENIED", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validPrefsPayload(ALICE_UID)));

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      appLanguage: "BENGALI",
      updatedAt: serverNow(),
    })
  );
});

test("Shadow update with ghost field or invalid enum fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const badPayload = { ...validPrefsPayload(ALICE_UID), isAdmin: true };
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(badPayload));

  const badEnum = { ...validPrefsPayload(ALICE_UID), appLanguage: "FRENCH" };
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(badEnum));
});

test("User cannot change immutable createdAt on update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(docRef.set(validPrefsPayload(ALICE_UID)));
  await assertFails(
    docRef.update({
      createdAt: serverNow(),
      updatedAt: serverNow(),
    })
  );
});

test("Prayer logs subcollection supports owner CRUD and list while blocking cross-user and unauthenticated", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();

  const aliceLogRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("prayerLogs")
    .doc("2026-11-16");

  const ts = serverNow();
  await assertSucceeds(
    aliceLogRef.set({
      userId: ALICE_UID,
      prayersCompleted: ["FAJR", "DHUHR"],
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("prayerLogs").get()
  );
  await assertSucceeds(
    aliceLogRef.update({
      prayersCompleted: ["FAJR", "DHUHR", "ASR"],
      updatedAt: serverNow(),
    })
  );

  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("prayerLogs").doc("2026-11-16").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("prayerLogs").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("prayerLogs").get()
  );
});
