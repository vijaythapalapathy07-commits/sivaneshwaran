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

test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Unauthenticated user: cannot read projects", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("projects").get());
});

test("Authenticated user: cannot read another user's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      email: "bob@example.com",
      displayName: "Bob",
      credits: 24,
      plan: "Free Plan",
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: can read and write own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice",
      credits: 24,
      plan: "Pro Plan",
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create and read own project", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("projects").doc("proj_1").set({
      userId: ALICE_UID,
      title: "Apex Dynamics",
      prompt: "Holographic glowing neon geometric letter L",
      engine: "text_to_logo",
      industry: "Tech & AI",
      style: "3D Emblem",
      badge: "VECTOR READY",
      variantsCount: 4,
      primaryColor: "#00E5FF",
      accentColor: "#A855F7",
      isPublic: false,
      likesCount: 0,
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );

  await assertSucceeds(aliceDb.collection("projects").doc("proj_1").get());
});

test("Authenticated user: cannot read another user's private project", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("projects").doc("bob_private").set({
      userId: BOB_UID,
      title: "Bob Private",
      prompt: "Secret logo",
      engine: "text_to_logo",
      industry: "Tech & AI",
      style: "Minimal",
      badge: "AI Gen v3",
      variantsCount: 2,
      primaryColor: "#FF0055",
      accentColor: "#00FFAA",
      isPublic: false,
      likesCount: 0,
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("projects").doc("bob_private").get());
});

test("Authenticated user: can read community public project", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("projects").doc("community_logo").set({
      userId: BOB_UID,
      title: "Vortex Labs",
      prompt: "Cybernetic glowing cube emblem",
      engine: "text_to_logo",
      industry: "Tech & AI",
      style: "3D Emblem",
      badge: "3D Emblem",
      variantsCount: 4,
      primaryColor: "#00E5FF",
      accentColor: "#3B82F6",
      isPublic: true,
      likesCount: 1420,
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("projects").doc("community_logo").get());
});

test("Query alignment: query own projects succeeds", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("projects").doc("alice_proj_2").set({
      userId: ALICE_UID,
      title: "Aero Crest",
      prompt: "Neon minimal eagle crest",
      engine: "vector_trace",
      industry: "Gaming & Esports",
      style: "Minimal",
      badge: "VECTOR READY",
      variantsCount: 4,
      primaryColor: "#A855F7",
      accentColor: "#EC4899",
      isPublic: true,
      likesCount: 892,
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("projects").where("userId", "==", ALICE_UID).get()
  );
});

test("Query alignment: query public projects succeeds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("projects").where("isPublic", "==", true).get()
  );
});
