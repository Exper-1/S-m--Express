const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_pilgrim_1";
const BOB_UID = "bob_pilgrim_2";

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

function validProfilePayload(uid) {
  const now = new Date(Date.now() - 1000);
  return {
    userId: uid,
    fullName: "Koffi Mensah",
    phone: "+229 01 97 11 22 33",
    tentNumber: "Tente B-14",
    sector: "Secteur Nord - Rangée 4",
    landmark: "Près de l'entrée Est",
    deliveryNotes: "Appeler à l'arrivée",
    latitude: 6.3703,
    longitude: 2.6166,
    preferredPayment: "MOBILE_MONEY",
    mobileMoneyNetwork: "MTN",
    notificationsEnabled: true,
    createdAt: now,
    updatedAt: now,
  };
}

function validOrderPayload(uid) {
  const now = new Date(Date.now() - 1000);
  return {
    userId: uid,
    orderNumber: "SE-2027-4821",
    vendorId: "vendor_saveurs_seme",
    vendorName: "Saveurs de Sèmè",
    itemsSummary: "1x Tilapia braisé & Alloco, 1x Jus de Bissap frais",
    itemCount: 2,
    subtotalFcfa: 2500,
    deliveryFeeFcfa: 300,
    totalFcfa: 2800,
    paymentMethod: "MOBILE_MONEY",
    mobileMoneyNetwork: "MTN",
    mobileMoneyNumber: "+229 01 97 11 22 33",
    status: "RECEIVED",
    courierName: "",
    courierPhone: "",
    tentNumber: "Tente B-14",
    sector: "Secteur Nord - Rangée 4",
    landmark: "Près de l'entrée Est",
    deliveryNotes: "Appeler à l'arrivée",
    contactPhone: "+229 01 97 11 22 33",
    latitude: 6.3703,
    longitude: 2.6166,
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated access is denied for profiles and orders", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("orders").get());
});

test("Authenticated owner can create and partially update own pilgrim profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID))
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      tentNumber: "Tente C-08",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost field & immutable createdAt update
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 400),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 300),
      updatedAt: new Date(Date.now() - 300),
    })
  );
});

test("Cross-user access to profile and orders is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("ord_1").set(validOrderPayload(ALICE_UID)));

  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("orders").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("orders").doc("ord_1").update({
    status: "ACCEPTED",
    updatedAt: new Date(Date.now() - 500),
  }));
});

test("Order creation, 8-step state transitions, and terminal DELIVERED lock", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const orderRef = aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("ord_1");

  await assertSucceeds(orderRef.set(validOrderPayload(ALICE_UID)));

  // Reject skipping stages (RECEIVED -> DELIVERED)
  await assertFails(orderRef.update({
    status: "DELIVERED",
    updatedAt: new Date(Date.now() - 800),
  }));

  // Valid sequential transitions
  const transitions = [
    { status: "ACCEPTED" },
    { status: "PREPARING" },
    { status: "READY" },
    { status: "COURIER_ASSIGNED", courierName: "Moussa Dossou", courierPhone: "+229 01 96 44 55 66" },
    { status: "PICKED_UP" },
    { status: "DELIVERING" },
    { status: "DELIVERED" },
  ];

  for (const step of transitions) {
    await assertSucceeds(orderRef.update({
      ...step,
      updatedAt: new Date(Date.now() - 200),
    }));
  }

  // Terminal state lock: once DELIVERED, no further updates allowed
  await assertFails(orderRef.update({
    status: "RECEIVED",
    updatedAt: new Date(Date.now() - 100),
  }));
});
