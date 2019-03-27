package a1;

import myGameEngine.*;
import networking.*;
import ray.rage.rendersystem.shader.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*; //for iterator
import java.lang.*;


import ray.rage.scene.controllers.RotationController;
import ray.rage.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rml.*;
import net.java.games.input.Event;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.asset.texture.*;
import ray.rage.asset.*;
import ray.input.*;
import ray.input.action.*;
import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.rage.util.BufferUtil;

import java.nio.*;
import java.net.UnknownHostException;

import ray.rage.asset.material.*;

import java.lang.Exception;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import ray.networking.IGameConnection.ProtocolType;


public class MyGame extends VariableFrameRateGame {

    GL4RenderSystem rs;
    private float elapsTime = 0.0f;
    private static float eTime;
    String timeLeftStr, scoreStr, dispStr, objTime;
    int TimeLeft, score = 0;
    int objSpecTime = 120;
    private InputManager im;
    private Action quitGameAction, moveBackwardAction, moveForwardAction, moveRightAction, moveLeftAction, incrementCounterAction, rotateUpAction, rotateDownAction, rotateRightAction, rotateLeftAction, toggleDolphinAction, moveSideAction, moveVerticleAction, rotateHorizontalAction, rotateVerticleAction;
    private static Camera camera;
    private static SceneNode dolphinN, cameraN, cameraNode1, specialItemN, rootNode;
    private Iterator<SceneNode> planetIterator;
    private SceneManager tempManager;
    private boolean earthFound, planet1Found, planet2Found, hasSpecialItem, win, loss;
    ManualObject specialItem;
    private static MyGame game;


    //Networking


    private String serverAddress;
    private int serverPort;
    private ProtocolType serverProtocol;
    private ProtocolClient protClient;
    private GameServerUDP gameServer;
    private boolean isClientConnected;
    private LinkedList<UUID> gameObjectsToRemove;

    private static String networkType; //going to need to be nonestatic at some point

//    private LinkedList<GhostAvatar> ghostAvatars; //taken from client

    public MyGame(String serverAddr, int sPort) {
        super();
        this.serverAddress = serverAddr;
        this.serverPort = sPort;
        this.serverProtocol = ProtocolType.UDP;
    }


    public static void main(String[] args) {
        game = new MyGame(args[0], Integer.parseInt(args[1]));
        networkType = args[2]; // s for server, c for client
        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            game.exit();
        }
    }

    private void setupNetworking() {
        gameObjectsToRemove = new LinkedList<UUID>();
        if (networkType.compareTo("s") == 0) { //server
            try {
                gameServer = new GameServerUDP(serverPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (networkType.compareTo("c") == 0) { //client
            isClientConnected = false;
            try {
                protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (protClient == null) {
                System.out.println("missing protocol host");
            } else { // ask client protocol to send initial join message
                ///to server, with a unique identifier for this client
                protClient.sendJoinMessage();
                Vector3f testPosVector = (Vector3f) Vector3f.createFrom(0.0f, 0.0f, 0.0f);
               // protClient.sendCreateMessage(testPosVector);
            }
        }

    }

    protected void processNetworking(float elapsTime) { // Process packets received by the client from the server
        if (protClient != null)
            protClient.processPackets();
// remove ghost avatars for players who have left the game
        Iterator<UUID> it = gameObjectsToRemove.iterator();
        while (it.hasNext()) {
            tempManager.destroySceneNode(it.next().toString());
        }
        gameObjectsToRemove.clear();
        for(UUID u: gameObjectsToRemove){
            System.out.println("game objects to remove contails: " + u);
        }
    }

    public Vector3 getPlayerPosition() {
        SceneNode dolphinN = tempManager.getSceneNode("myDolphinNode");
        return dolphinN.getWorldPosition();
    }

    public void addGhostAvatarToGameWorld(GhostAvatar avatar) throws IOException {
        if (avatar != null) {
            Entity ghostE = tempManager.createEntity("ghost", "dolphinHighPoly.obj");
            ghostE.setPrimitive(Primitive.TRIANGLES);
            SceneNode ghostN = tempManager.getRootSceneNode().createChildSceneNode(avatar.getId().toString());
            ghostN.attachObject(ghostE);
            ghostN.setLocalPosition(avatar.getGhostPosition()); //get position that was sent
            avatar.setNode(ghostN);
            avatar.setEntity(ghostE);
           // avatar.setPosition(node’s position...maybe redundant);
        }
    }
//
    public void removeGhostAvatarFromGameWorld(GhostAvatar avatar) {
        if (avatar != null) gameObjectsToRemove.add(avatar.getId());
        for(UUID u: gameObjectsToRemove){
            System.out.println("will remove: " + u);
        }
    }

    private class SendCloseConnectionPacketAction extends AbstractInputAction { // for leaving the game... need to attach to an input device
        @Override
        public void performAction(float time, Event evt) {
            if (protClient != null && isClientConnected == true) {
                protClient.sendByeMessage();
            }
        }

    }


    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
        rootNode = sm.getRootSceneNode();
        camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(camera);

        camera.setRt((Vector3f) Vector3f.createFrom(1.0f, 0.0f, 0.0f));
        camera.setUp((Vector3f) Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        camera.setFd((Vector3f) Vector3f.createFrom(0.0f, 0.0f, -1.0f));

        camera.setPo((Vector3f) Vector3f.createFrom(0.0f, 0.0f, 0.0f));

        dolphinN = sm.getRootSceneNode().createChildSceneNode("myDolphinNode");
        dolphinN.attachObject(camera);
        camera.setMode('r');

    }

    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {

        /*Entities    */
        earthFound = false;
        planet1Found = false;
        planet2Found = false;
        hasSpecialItem = false;
        win = false;
        loss = true;
        Entity dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
        dolphinE.setPrimitive(Primitive.TRIANGLES);


        Entity earthE = sm.createEntity("earth", "earth.obj");

        Entity planetE = sm.createEntity("planet", "sphere.obj");
        TextureManager tm = eng.getTextureManager();
        Texture moonTexture = tm.getAssetByPath("moon.jpeg");
        RenderSystem rs = sm.getRenderSystem();
        TextureState state = (TextureState) rs.createRenderState(RenderState.Type.TEXTURE);
        state.setTexture(moonTexture);
        planetE.setRenderState(state);

        Entity planet2E = sm.createEntity("planet2", "sphere.obj");


        Texture planet2Texture = tm.getAssetByPath("blue.jpeg");

        TextureState p2State = (TextureState) rs.createRenderState(RenderState.Type.TEXTURE);
        p2State.setTexture(planet2Texture);
        planet2E.setRenderState(p2State);




        /*Nodes */


        dolphinN.moveBackward(2.0f);
        dolphinN.attachObject(dolphinE);
        dolphinN.yaw((Degreef.createFrom(125.0f)));

        //working here
        cameraN = dolphinN.createChildSceneNode("cameraNode");
        cameraN.attachObject(camera);

        //set rotation

        Vector3f v = camera.getUp();
        Vector3f p = camera.getPo();


        Vector3f p1 = (Vector3f) Vector3f.createFrom(1.0f * v.x(), 1.0f * v.y(), 3.0f);
        Vector3f p2 = (Vector3f) p.add((Vector3) p1);
        cameraN.moveUp(0.3f);
        Vector3f cp = (Vector3f) cameraN.getLocalPosition();
        //turn dolphin around
        //cameraN.setLocalPosition(cp.x(),cp.y(),-cp.z());


        SceneNode earthN = sm.getRootSceneNode().createChildSceneNode("EarthNode");
        earthN.moveBackward(30f);
        earthN.moveRight(20f);
        earthN.attachObject(earthE);
        earthN.scale(0.8f, 0.8f, 0.8f);

        SceneNode planet2N = sm.getRootSceneNode().createChildSceneNode("planetNode");
        planet2N.moveBackward(15.0f);
        //planet2N.moveRight((float)Math.random()*20.0f);
        //planet2N.moveLeft((float)Math.random()*2.0f);
        planet2N.attachObject(planetE);
        planet2N.scale(1.1f, 1.1f, 1.1f);


        SceneNode planet3N = sm.getRootSceneNode().createChildSceneNode("planet2Node");
        //planet3N.moveBackward(((float)Math.random()*10.0f) + 6.0f);
        // planet3N.moveLeft((float)Math.random()*15.0f);
        planet3N.moveRight(20.0f);
        planet3N.attachObject(planet2E);
        planet3N.scale(1.8f, 1.8f, 1.8f);

        specialItemN = dolphinN.createChildSceneNode("SpecialItemNode");

        sm.getAmbientLight().setIntensity(new Color(.5f, .5f, .5f));


        Light plight = sm.createLight("testLamp1", Light.Type.POINT);
        plight.setAmbient(new Color(.3f, .3f, .3f));
        plight.setDiffuse(new Color(.7f, .7f, .7f));
        plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(5f);

        SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);

        // make manual objects – in this case a pyramid
        specialItem = makePyramid(eng, sm);
        specialItemN.scale(0.1f, 0.1f, 0.1f);
        specialItemN.moveForward(1.5f);
        specialItemN.moveUp(0.6f);

//        specialItemN.attachObject(specialItem);
//        specialItemN.moveForward(1.5f);
//        specialItemN.moveUp(0.6f);


        ManualObject shape = makeShape(eng, sm);
        SceneNode shapeN = sm.getRootSceneNode().createChildSceneNode("ShapeNode");
        shapeN.scale(0.75f, 0.75f, 0.75f);
        shapeN.attachObject(shape);

        //coordinates
        ManualObject xLine = makeXLine(eng, sm);
        SceneNode xLineN = sm.getRootSceneNode().createChildSceneNode("XAxisNode");
        xLineN.attachObject(xLine);


        ManualObject yLine = makeYLine(eng, sm);
        SceneNode yLineN = sm.getRootSceneNode().createChildSceneNode("YAxisNode");
        yLineN.attachObject(yLine);

        ManualObject zLine = makeZLine(eng, sm);
        SceneNode zLineN = sm.getRootSceneNode().createChildSceneNode("ZAxisNode");
        zLineN.attachObject(zLine);


        RotationController rc = new RotationController(Vector3f.createUnitVectorY(), 0.02f);
        rc.addNode(earthN);
        sm.addController(rc);
        setupInputs();
        //getsceneNodes
        tempManager = sm;
        objSpecTime = 10000;   //set time to win to 70

        setupNetworking();


    }

    @Override
    protected void update(Engine engine) {
        processNetworking(elapsTime);
        rs = (GL4RenderSystem) engine.getRenderSystem();
        checkNodeDistance();
        eTime = engine.getElapsedTimeMillis();
        elapsTime += engine.getElapsedTimeMillis();
        TimeLeft = (objSpecTime - Math.round(elapsTime / 1000.0f));
        scoreStr = Integer.toString(score);

        timeLeftStr = Integer.toString(TimeLeft);
        objTime = Integer.toString(objSpecTime);

        dispStr = "Countdown: " + (timeLeftStr) + " Score: " + scoreStr;
        if (win == false) {
            rs.setHUD(dispStr, 15, 15);
        }
        if (TimeLeft <= 0 && loss == true) {
            rs.setHUD("You are out of time! Better luck next time", 15, 15);
            hasSpecialItem = false; // so you can't win in the last two seconds
        }
        if (TimeLeft <= -2 && loss == true) {
            this.shutdown();

        }

        dolphinN.update();
        im.update(elapsTime);

        if (getDolphinCameraDistance() > 1.5f && camera.getMode() == 'c') {
            camera.setMode('r');
        }


    }


    private void checkNodeDistance() {
        planetIterator = tempManager.getSceneNodes().iterator();
        while (planetIterator.hasNext()) {


            SceneNode tempNode = planetIterator.next();
            String pName = tempNode.getName();
            //if(pName.equals("EarthNode") || pName.equals("planetNode" ){
            Vector3f tempNodeP = (Vector3f) tempNode.getWorldPosition();
            Vector3f dolphinP = camera.getPo();
            ;
            Vector3f subPos = (Vector3f) dolphinP.sub(tempNodeP);
            Vector3f rootP = (Vector3f) rootNode.getWorldPosition();
            Vector3f subPosItem = (Vector3f) rootP.sub(tempNodeP);
            float squared = (float) Math.pow(subPos.x(), 2) + (float) Math.pow(subPos.y(), 2) + (float) Math.pow(subPos.z(), 2);
            float distResult = (float) Math.sqrt(squared);


            //distance between special item and root node

            float squaredItem = (float) Math.pow(subPosItem.x(), 2) + (float) Math.pow(subPosItem.y(), 2) + (float) Math.pow(subPosItem.z(), 2);
            float distItem = (float) Math.sqrt(squaredItem);


            if (distResult < 2.0f || distItem < 0.5f) { //check planet distance and distance from manual object


                if (pName.equals("EarthNode") && earthFound == false && planet1Found == true && planet2Found == true) {
                    specialItemN.attachObject(specialItem);
                    score++;
                    hasSpecialItem = true;
                    earthFound = true;


                }
                if (pName.equals("planetNode") && planet1Found == false) {
                    score++;
                    planet1Found = true;

                }
                if (pName.equals("planet2Node") && planet2Found == false) {
                    score++;
                    planet2Found = true;
                }

                if (pName.equals("SpecialItemNode") && hasSpecialItem && distItem < 0.5f) {
                    specialItemN.detachObject(specialItem);
                    score += 100;
                    rs.setHUD("You Win!", 15, 15);
                    win = true;
                    hasSpecialItem = false;
                    loss = false; //free to roam


                }

            }
        }

    }


    public void incrementCounter() {
        score++;
    }

    public void shutdown() {
        System.out.println("shutdown requested");
        protClient.sendByeMessage();
        game.setState(Game.State.STOPPING);
    }


    //magnitude of distance between camera and dolphin
    public static float getDolphinCameraDistance() {
        Vector3f cameraP = camera.getPo();
        Vector3f dolphinP = (Vector3f) dolphinN.getWorldPosition();
        Vector3f subPos = (Vector3f) dolphinP.sub(cameraP);
        float squared = (float) Math.pow(subPos.x(), 2) + (float) Math.pow(subPos.y(), 2) + (float) Math.pow(subPos.z(), 2);
        float distResult = (float) Math.sqrt(squared);
        return distResult;

    }

    public static float getFrameTime() {
        return eTime;
    }


    protected void setupInputs() {

        im = new GenericInputManager();
        String kbName = im.getKeyboardName();
       // String gpName = im.getFirstGamepadName();

        System.out.println(kbName);
        dolphinN.moveRight(3);

        quitGameAction = new QuitGameAction(this);
        incrementCounterAction = new IncrementCounterAction(this);

        //movement
        moveForwardAction = new ActionMoveForward(camera, dolphinN);
        moveRightAction = new ActionMoveRight(camera, dolphinN);
        moveLeftAction = new ActionMoveLeft(camera, dolphinN);
        moveBackwardAction = new ActionMoveBackward(camera, dolphinN);
        moveSideAction = new ActionSideMove(camera, dolphinN);
        moveVerticleAction = new ActionVerticleMove(camera, dolphinN);


        //rotation
        rotateUpAction = new ActionRotateUp(camera, dolphinN);
        rotateDownAction = new ActionRotateDown(camera, dolphinN);
        rotateRightAction = new ActionRotateRight(camera, dolphinN);
        rotateLeftAction = new ActionRotateLeft(camera, dolphinN);
        rotateHorizontalAction = new ActionRotateHorizontal(camera, dolphinN);
        rotateVerticleAction = new ActionRotateVerticle(camera, dolphinN);


        toggleDolphinAction = new ActionToggleDolphin(camera, dolphinN);


        im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.Y, quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
        im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.C, incrementCounterAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
//        im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._0, incrementCounterAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
//        //Movement
        im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.W, moveForwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.D, moveRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
//        im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.X, moveSideAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.A, moveLeftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.S, moveBackwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
//        im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.Y, moveVerticleAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        //camera rotation
        im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.UP, rotateUpAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.DOWN, rotateDownAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.RIGHT, rotateRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.LEFT, rotateLeftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.SPACE, toggleDolphinAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
//        im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._0, toggleDolphinAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
//        im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.RX, rotateHorizontalAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
//        im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.RY, rotateVerticleAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);


    }


    //used from his notes for extra activity object
    protected ManualObject makePyramid(Engine eng, SceneManager sm) throws IOException {
        ManualObject pyr = sm.createManualObject("Pyramid");
        ManualObjectSection pyrSec = pyr.createManualSection("PyramidSection");
        pyr.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
        float[] vertices = new float[]
                {-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, //front
                        1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, //right
                        1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, //back
                        -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, //left
                        -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
                        1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f //RR
                };


        float[] texcoords = new float[]
                {0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f,
                        0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                        1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
                };
        float[] normals = new float[]
                {0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f,
                        -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f
                };
        int[] indices = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};

        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
        FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
        pyrSec.setVertexBuffer(vertBuf);
        pyrSec.setTextureCoordsBuffer(texBuf);
        pyrSec.setNormalsBuffer(normBuf);
        pyrSec.setIndexBuffer(indexBuf);
        Texture tex = eng.getTextureManager().getAssetByPath("chain-fence.jpeg");
        TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        texState.setTexture(tex);
        FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
        pyr.setDataSource(DataSource.INDEX_BUFFER);
        pyr.setRenderState(texState);
        pyr.setRenderState(faceState);
        return pyr;
    }

    protected ManualObject makeShape(Engine eng, SceneManager sm) throws IOException {
        ManualObject shape = sm.createManualObject("Shape");
        ManualObjectSection shapeSec = shape.createManualSection("ShapeSection");
        shape.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
        //shape
        float[] vertices = new float[]
                {-1.0f, 1.0f, 0.5f, 1.0f, 1.0f, 0.5f, 0.0f, 2.0f, 0.0f,//topfront
                        1.0f, 1.0f, 0.5f, 1.0f, 1.0f, -0.5f, 0.0f, 2.0f, 0.0f,//topright
                        1.0f, 1.0f, -0.5f, -1.0f, 1.0f, -0.5f, 0.0f, 2.0f, 0.0f,//top back
                        -1.0f, 1.0f, -0.5f, -1.0f, 1.0f, 0.5f, 0.0f, 2.0f, 0.0f,//topLeft
                        1.0f, 1.0f, 0.5f, -1.0f, 1.0f, 0.5f, 0.0f, -2.0f, 0.0f,//bottomfront
                        1.0f, 1.0f, -0.5f, 1.0f, 1.0f, 0.5f, 0.0f, -2.0f, 0.0f,//bottomright
                        -1.0f, 1.0f, -0.5f, 1.0f, 1.0f, -0.5f, 0.0f, -2.0f, 0.0f,//bottom back
                        -1.0f, 1.0f, 0.5f, -1.0f, 1.0f, -0.5f, 0.0f, -2.0f, 0.0f//bottomLeft


                };
        float[] texcoords = new float[]
                {0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //topfront
                        0.25f, 0.0f, 0.75f, 0.0f, 0.5f, 1.0f, //topright
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //topback
                        0.25f, 0.0f, 0.75f, 0.0f, 0.5f, 1.0f, //topleft
                        0.25f, 0.0f, 0.75f, 0.0f, 0.5f, 1.0f, //bottomfront
                        0.4f, 0.0f, 0.6f, 0.0f, 0.5f, 1.0f, //bottomright
                        0.25f, 0.0f, 0.75f, 0.0f, 0.5f, 1.0f, //bottomback
                        0.4f, 0.0f, 0.6f, 0.0f, 0.5f, 1.0f //boottomleft


                };
        float[] normals = new float[]
                {0.0f, 2.0f, 0.5f, 0.0f, 2.0f, 0.5f, 0.0f, 2.0f, 0.5f,//topfront
                        1.0f, 2.0f, 0.0f, 1.0f, 2.0f, 0.0f, 1.0f, 2.0f, 0.0f,//topright
                        0.0f, 2.0f, -0.5f, 0.0f, 2.0f, -0.5f, 0.0f, 2.0f, -0.5f,//topback
                        -1.0f, 2.0f, 0.0f, -1.0f, 2.0f, 0.0f, -1.0f, 2.0f, 0.0f, //topleft
                        0.0f, 0.0f, 0.5f, 0.0f, 0.0f, 0.5f, 0.0f, 0.0f, 0.5f,//bottomfront
                        1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,//bottomright
                        0.0f, 0.0f, -0.5f, 0.0f, 0.0f, -0.5f, 0.0f, 0.0f, -0.5f,//bottomback
                        -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f //bottomleft


                };
        int[] indices = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};//,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41};

        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
        FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
        shapeSec.setVertexBuffer(vertBuf);
        shapeSec.setTextureCoordsBuffer(texBuf);
        shapeSec.setNormalsBuffer(normBuf);
        shapeSec.setIndexBuffer(indexBuf);
        Texture tex = eng.getTextureManager().getAssetByPath("hexagons.jpeg");
        TextureState texState = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        texState.setTexture(tex);
        Material mat1 = sm.getMaterialManager().getAssetByPath("cube.mtl");
        mat1.setEmissive(Color.WHITE);
        shapeSec.setMaterial(mat1);
        FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
        shape.setDataSource(DataSource.INDEX_BUFFER);
        shape.setRenderState(texState);
        shape.setRenderState(faceState);
        return shape;
    }

    protected ManualObject makeXLine(Engine eng, SceneManager sm) throws IOException {
        ManualObject line = sm.createManualObject("Line");
        ManualObjectSection lineSec = line.createManualSection("LineSection");
        line.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
        SceneNode xNode = sm.getRootSceneNode();


        float[] vertices = new float[]
                {-1000.0f, 0.0f, 0.0f,
                        1000.0f, 0.0f, 0.0f

                };
        int indices[] = new int[]{0, 1};
        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
        lineSec.setVertexBuffer(vertBuf);
        lineSec.setIndexBuffer(indexBuf);


        Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");

        mat.setEmissive(Color.BLUE);
        Texture tex = eng.getTextureManager().getAssetByPath("bright-red.jpeg");
        TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);

        tstate.setTexture(tex);

        lineSec.setRenderState(tstate);

        lineSec.setMaterial(mat);
        line.setPrimitive(Primitive.LINES);
        line.setPrimitive(Primitive.LINES);
        return line;


    }

    protected ManualObject makeYLine(Engine eng, SceneManager sm) throws IOException {
        ManualObject yLine = sm.createManualObject("YLine");
        ManualObjectSection yLineSec = yLine.createManualSection("YLineSection");
        yLine.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
        SceneNode yNode = sm.getRootSceneNode();


        float[] vertices = new float[]
                {0.0f, -1000.0f, 0.0f,
                        0.0f, 1000.0f, 0.0f

                };
        int indices[] = new int[]{0, 1};
        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
        yLineSec.setVertexBuffer(vertBuf);
        yLineSec.setIndexBuffer(indexBuf);


        Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");

        mat.setEmissive(Color.BLUE);
        Texture tex = eng.getTextureManager().getAssetByPath("bright-blue.jpeg");
        TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);

        tstate.setTexture(tex);

        yLineSec.setRenderState(tstate);

        yLineSec.setMaterial(mat);
        yLine.setPrimitive(Primitive.LINES);
        return yLine;


    }

    protected ManualObject makeZLine(Engine eng, SceneManager sm) throws IOException {
        ManualObject zLine = sm.createManualObject("ZLine");
        ManualObjectSection zLineSec = zLine.createManualSection("ZLineSection");
        zLine.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
        SceneNode zNode = sm.getRootSceneNode();

        float[] vertices = new float[]
                {0.0f, 0.0f, 1000.0f,
                        0.0f, 0.0f, -1000.0f

                };
        int indices[] = new int[]{0, 1};
        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
        zLineSec.setVertexBuffer(vertBuf);
        zLineSec.setIndexBuffer(indexBuf);


        Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");

        mat.setEmissive(Color.BLUE);
        Texture tex = eng.getTextureManager().getAssetByPath("bright-green.jpeg");
        TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);

        tstate.setTexture(tex);

        zLineSec.setRenderState(tstate);

        zLineSec.setMaterial(mat);
        zLine.setPrimitive(Primitive.LINES);
        zLine.setPrimitive(Primitive.LINES);
        return zLine;


    }
}
