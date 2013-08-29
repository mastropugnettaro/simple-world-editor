/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.swe.managers;

import com.swe.transform.EditorTransformConstraint;
import com.swe.transform.EditorTransformManager;
import com.swe.entitysystem.EntityNameComponent;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Line;
import com.swe.EditorBaseManager;
import com.swe.scene.EditorLayersGroupObject;
import com.swe.scene.EditorSceneObject;
import com.swe.selection.EditorSelectionManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.NiftyMouse;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.RadioButtonGroupStateChangedEvent;
import de.lessvoid.nifty.controls.TabGroup;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.controls.TreeBox;
import de.lessvoid.nifty.controls.TreeItem;
import de.lessvoid.nifty.controls.nullobjects.CheckBoxNull;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.events.ElementEnableEvent;
import de.lessvoid.nifty.elements.events.NiftyMousePrimaryClickedEvent;
import de.lessvoid.nifty.input.NiftyMouseInputEvent;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mifth
 */
public class EditorGuiManager extends AbstractAppState implements ScreenController {

    private Screen screen;
    private static Nifty nifty;
    private SimpleApplication application;
    private Node gridNode, rootNode, guiNode;
    private AssetManager assetManager;
    private ViewPort guiViewPort;
    private EditorBaseManager base;
    private Element popupMoveToLayer, popupEditComponent, popupEditAsset, rightPanel;
    private ListBox entitiesListBox, sceneObjectsListBox, componentsListBox, assetsListBox, scenesListbox, layersGroupsListbox;
    private long lastIdOfComponentList, idComponentToChange;

    public EditorGuiManager(EditorBaseManager base) {
        this.base = base;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {

        super.initialize(stateManager, app);
        application = (SimpleApplication) app;
        rootNode = application.getRootNode();
        assetManager = app.getAssetManager();
        guiNode = application.getGuiNode();
        guiViewPort = application.getGuiViewPort();

        createGrid();

        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(application.getAssetManager(),
                application.getInputManager(),
                application.getAudioRenderer(),
                guiViewPort);

        nifty = niftyDisplay.getNifty();
        nifty.fromXml("Interface/Main/basicGui.xml", "start", this);

        // attach the nifty display to the gui view port as a processor
        guiViewPort.addProcessor(niftyDisplay);
        application.getInputManager().setCursorVisible(true);

//        // Set Logger for only warnings     
//        Logger root = Logger.getLogger("");
//        Handler[] handlers = root.getHandlers();
//        for (int i = 0; i < handlers.length; i++) {
//            if (handlers[i] instanceof ConsoleHandler) {
//                ((ConsoleHandler) handlers[i]).setLevel(Level.WARNING);
//            }
//        }

        // set popup test
        popupMoveToLayer = nifty.createPopup("popupMoveToLayer");
        popupMoveToLayer.disable();
        screen.getFocusHandler().resetFocusElements();

        // set popup test
        popupEditComponent = nifty.createPopup("popupEditComponent");
        popupEditComponent.disable();
        screen.getFocusHandler().resetFocusElements();

        // set popup test
        popupEditAsset = nifty.createPopup("popupEditAsset");
        popupEditAsset.disable();
        screen.getFocusHandler().resetFocusElements();

        // ListBoxes
        scenesListbox = screen.findNiftyControl("scenesListBox", ListBox.class);
        layersGroupsListbox = screen.findNiftyControl("layersGroupsListBox", ListBox.class);
        entitiesListBox = screen.findNiftyControl("entitiesListBox", ListBox.class);
        sceneObjectsListBox = screen.findNiftyControl("sceneObjectsListBox", ListBox.class);
        sceneObjectsListBox.changeSelectionMode(ListBox.SelectionMode.Multiple, false);
        componentsListBox = screen.findNiftyControl("componentsListBox", ListBox.class);
        assetsListBox = screen.findNiftyControl("assetsListBox", ListBox.class);

        // rightPanel
        rightPanel = screen.findElementByName("settingsRightPanel");

        //TempAssetsString
        assetsListBox.addItem("/home/mifth/jMonkeyProjects/AD/ad/trunk/ADAssets/assets");

        nifty.gotoScreen("start"); // start the screen 

        updateSceneGUI();


//        // test for treebox
//        NiftyImage folder = nifty.createImage("Interface/Textures/closed.png", true);
//        NiftyImage folderOpen = nifty.createImage("Interface/Textures/opened.png", true);
//        NiftyImage item = nifty.createImage("Interface/Textures/closed.png", true);
//
//        TreeItem<String> treeRoot = new TreeItem<String>();
//        TreeItem<String> branch1 = new TreeItem<String>(treeRoot, "branch 1", "branche 1", folder, folderOpen, true);
//        TreeItem<String> branch11 = new TreeItem<String>(treeRoot, "branch 1 1", "branche 1 1", item);
//        TreeItem<String> branch12 = new TreeItem<String>(treeRoot, "branch 1 2", "branche 1 2", item);
//        branch1.addTreeItem(branch11);
//        branch1.addTreeItem(branch12);
//        TreeItem<String> branch2 = new TreeItem<String>(treeRoot, "branch 2", "branche 2", folder, folderOpen, true);
//        TreeItem<String> branch21 = new TreeItem<String>(treeRoot, "branch 2 1", "branche 2 1", folder, folderOpen, true);
//        TreeItem<String> branch211 = new TreeItem<String>(treeRoot, "branch 2 1 1", "branche 2 1 1", item);
//        branch2.addTreeItem(branch21);
//        branch21.addTreeItem(branch211);
//        treeRoot.addTreeItem(branch1);
//        treeRoot.addTreeItem(branch2);
//        branch1.setExpanded(false);
//
//        screen.findNiftyControl("researchTree", TreeBox.class).setTree(treeRoot);



        screen.getFocusHandler().resetFocusElements();
    }

//    public void temp() {
//        TreeBox treeBox = screen.findNiftyControl("researchTree", TreeBox.class);
//        ListBox listboxOfTreebox = treeBox.getElement().findNiftyControl("#listbox", ListBox.class);
//        if (listboxOfTreebox.getSelection().size() > 0) {
//            System.out.println(listboxOfTreebox.getSelection().get(0));
//        }
//        
//    }
    private void updateSceneGUI() {

        // savePreviewj3o checkbox
        CheckBox cbPreview = screen.findNiftyControl("savePreviewJ3O", CheckBox.class);
        updateCheckbox(base.getSceneManager().getSavePreviewJ3o(), "savePreviewJ3O");

        // set Scenes ListBox
        scenesListbox.clear();
        EditorSceneObject activeScene = base.getSceneManager().getActiveScene();
        String activeSceneName = activeScene.getSceneName();
        for (String sceneName : base.getSceneManager().getScenesList().keySet()) {
            scenesListbox.addItem(sceneName);
            if (sceneName.equals(activeSceneName)) {
                scenesListbox.selectItem(sceneName);
            }
        }
        scenesListbox.sortAllItems();
        scenesListbox.refresh();

        // set checkbox isEnabled scene
        boolean isSceneEnabled = (Boolean) activeScene.getSceneNode().getUserData("isEnabled");
        updateCheckbox(isSceneEnabled, "isSceneEnabled");

        // set LayersGroups ListBox
        layersGroupsListbox.clear();
        EditorLayersGroupObject activeLayersGroup = base.getSceneManager().getActiveScene().getActivelayersGroup();
        String activeLayerGroupName = activeLayersGroup.getLayersGroupName();
        for (String layerGroupName : base.getSceneManager().getActiveScene().getLayerGroupsList().keySet()) {
            layersGroupsListbox.addItem(layerGroupName);
            if (layerGroupName.equals(activeLayerGroupName)) {
                layersGroupsListbox.selectItem(layerGroupName);
            }
        }
        layersGroupsListbox.sortAllItems();
        layersGroupsListbox.refresh();

        // set checkbox LayersGroup 
        boolean isLayersGroupEnabled = (Boolean) activeLayersGroup.getLayersGroupNode().getUserData("isEnabled");
        updateCheckbox(isLayersGroupEnabled, "isLayersGroupEnabled");

        updateLayersGUI();
    }

    private void updateLayersGUI() {
        // set checkboxes for layers
        for (int i = 0; i < 20; i++) {
            Node layer = base.getSceneManager().getActiveScene().getActivelayersGroup().getLayer(i + 1);

            // LAYERS VISIBILITY
            CheckBox cbVisible = screen.findNiftyControl("layerVisibility" + (i + 1), CheckBox.class);
            Object isEnabledObj = layer.getUserData("isEnabled");
            boolean isEnabled = (Boolean) isEnabledObj;

            if (isEnabled) {
                cbVisible.check();
            } else {
                cbVisible.uncheck();
            }


            // LAYERS LOCK
            CheckBox cbLocked = screen.findNiftyControl("layerLock" + (i + 1), CheckBox.class);
            Object isLockedObj = layer.getUserData("isLocked");
            boolean isLocked = (Boolean) isLockedObj;

            if (isLocked) {
                cbLocked.check();
            } else {
                cbLocked.uncheck();
            }

            // deselect Red Color of all LayersVisibility
            Element deselectImageVisibility = screen.findElementByName("layerVisibility" + (i + 1));
            deselectImageVisibility.stopEffect(EffectEventId.onFocus);
            Element deselectImageLock = screen.findElementByName("layerLock" + (i + 1));
            deselectImageLock.stopEffect(EffectEventId.onFocus);

        }

        // SET THE LAYER ACTIVE (Red color)
        Node activeLayer = base.getSceneManager().getActiveScene().getActivelayersGroup().getActiveLayer();
        if (activeLayer != null) {
            screen.getFocusHandler().resetFocusElements();
            int activeLayerNumb = (Integer) base.getSceneManager().getActiveScene().getActivelayersGroup().getActiveLayer().getUserData("LayerNumber");
            Element selectImage = screen.findElementByName("layerVisibility" + activeLayerNumb);
            selectImage.startEffect(EffectEventId.onFocus);
        }
        screen.getFocusHandler().resetFocusElements();
    }

    private void updateCheckbox(boolean setBoolean, String checkboxID) {
        CheckBox cbScene = screen.findNiftyControl(checkboxID, CheckBox.class);

        if (setBoolean) {
            cbScene.check();
        } else {
            cbScene.uncheck();
        }
    }

    protected void clearGui() {
        // clear gui lists
        scenesListbox.clear();
        layersGroupsListbox.clear();
        entitiesListBox.clear();
        sceneObjectsListBox.clear();
        componentsListBox.clear();

        // clear layers
        for (int i = 0; i < 20; i++) {
            CheckBox cb = screen.findNiftyControl("layerVisibility" + (i + 1), CheckBox.class);
            cb.uncheck();
            Element selectActiveLayerImage = screen.findElementByName("layerVisibility" + (i + 1));
            selectActiveLayerImage.stopEffect(EffectEventId.onFocus);
            selectActiveLayerImage.startEffect(EffectEventId.onEnabled);
        }
        // clear layers locks
        for (int i = 0; i < 20; i++) {
            CheckBox cb = screen.findNiftyControl("layerLock" + (i + 1), CheckBox.class);
            if (cb.isChecked()) {
                cb.uncheck();
            }
        }


        // clear assets
        assetsListBox.clear();

        screen.getFocusHandler().resetFocusElements();
    }

    public static Nifty getNifty() {
        return nifty;
    }

    /**
     * This is called when the RadioButton selection has changed.
     */
    @NiftyEventSubscriber(id = "RadioGroupConstraints")
    public void RadioGroupConstraintsChanged(final String id, final RadioButtonGroupStateChangedEvent event) {

        if (event.getSelectedId().equals("constraint_none")) {
            base.getTransformManager().getConstraintTool().setConstraint(0.0f);
            screen.getFocusHandler().resetFocusElements();
        } else if (event.getSelectedId().equals("constraint_0.5")) {
            base.getTransformManager().getConstraintTool().setConstraint(0.5f);
            screen.getFocusHandler().resetFocusElements();
        } else if (event.getSelectedId().equals("constraint_1")) {
            base.getTransformManager().getConstraintTool().setConstraint(1.0f);
            screen.getFocusHandler().resetFocusElements();
        } else if (event.getSelectedId().equals("constraint_5")) {
            base.getTransformManager().getConstraintTool().setConstraint(5.0f);
            screen.getFocusHandler().resetFocusElements();
        } else if (event.getSelectedId().equals("constraint_10")) {
            base.getTransformManager().getConstraintTool().setConstraint(10.0f);
            screen.getFocusHandler().resetFocusElements();
        }
    }

    /**
     * This is called when the RadioButton selection has changed.
     */
    @NiftyEventSubscriber(id = "RadioGroupSelection")
    public void RadioGroupSelectionChanged(final String id, final RadioButtonGroupStateChangedEvent event) {

        if (event.getSelectedId().equals("mouse_sel")) {
            setMouseSelection();
            screen.getFocusHandler().resetFocusElements();
        } else if (event.getSelectedId().equals("rectangle_sel")) {
            setRectangleSelection();
            screen.getFocusHandler().resetFocusElements();
        }
    }

    /**
     * This is called when the RadioButton selection has changed.
     */
    @NiftyEventSubscriber(id = "RadioGroupSelectionAdditive")
    public void RadioGroupSelectionAdditiveChanged(final String id, final RadioButtonGroupStateChangedEvent event) {

        if (event.getSelectedId().equals("normal_sel")) {
            setNormalSelection();
            screen.getFocusHandler().resetFocusElements();
        } else if (event.getSelectedId().equals("additive_sel")) {
            setAdditiveSelection();
            screen.getFocusHandler().resetFocusElements();
        }
    }

//    // for sceneObjectsListBox manipulation
//    @NiftyEventSubscriber(id = "sceneObjectsListBox")
//    public void onListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent changed) {
//    }
    public void setMoveManipulator() {
        System.out.println("Manipulator is changed");
        base.getTransformManager().setTransformType(EditorTransformManager.TransformToolType.MoveTool);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setRotateManipulator() {
        System.out.println("Manipulator is changed");
        base.getTransformManager().setTransformType(EditorTransformManager.TransformToolType.RotateTool);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setScaleManipulator() {
        System.out.println("Manipulator is changed");
        base.getTransformManager().setTransformType(EditorTransformManager.TransformToolType.ScaleTool);
        screen.getFocusHandler().resetFocusElements();
    }

    public void clearTransform(String transformType) {
        for (Long id : base.getSelectionManager().getSelectionList()) {
            Node entity = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
            if (transformType.equals("Translation")) {
                entity.setLocalTranslation(new Vector3f());
            } else if (transformType.equals("Rotation")) {
                entity.setLocalRotation(new Quaternion());
            } else if (transformType.equals("Scale")) {
                entity.setLocalScale(new Vector3f(1, 1, 1));
            }

        }
        base.getSelectionManager().calculateSelectionCenter();

        // set history
        base.getHistoryManager().prepareNewHistory();
        base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());
        base.getHistoryManager().getHistoryList().get(base.getHistoryManager().getHistoryCurrentNumber()).setDoTransform(true);
        screen.getFocusHandler().resetFocusElements();
    }

    public void snapObjectsToGrid() {
        for (Long id : base.getSelectionManager().getSelectionList()) {
            Node entity = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();

            float constrX = base.getTransformManager().getConstraintTool().constraintValue(entity.getLocalTranslation().getX());
            float constrY = base.getTransformManager().getConstraintTool().constraintValue(entity.getLocalTranslation().getY());
            float constrZ = base.getTransformManager().getConstraintTool().constraintValue(entity.getLocalTranslation().getZ());

            entity.setLocalTranslation(constrX, constrY, constrZ);
            base.getSelectionManager().calculateSelectionCenter();

            // set history
            base.getHistoryManager().prepareNewHistory();
            base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());
            base.getHistoryManager().getHistoryList().get(base.getHistoryManager().getHistoryCurrentNumber()).setDoTransform(true);
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void setGrid() {
        int indexGrid = rootNode.getChildIndex(gridNode);
        if (indexGrid == -1) {
            rootNode.attachChild(gridNode);
        } else {
            rootNode.detachChild(gridNode);
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void setMouseSelection() {
        base.getSelectionManager().setSelectionTool(EditorSelectionManager.SelectionToolType.MouseClick);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setRectangleSelection() {
        base.getSelectionManager().setSelectionTool(EditorSelectionManager.SelectionToolType.Rectangle);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setLocalCoords() {
        base.getTransformManager().setTrCoordinates(EditorTransformManager.TransformCoordinates.LocalCoords);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setWorldCoords() {
        base.getTransformManager().setTrCoordinates(EditorTransformManager.TransformCoordinates.WorldCoords);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setViewCoords() {
        base.getTransformManager().setTrCoordinates(EditorTransformManager.TransformCoordinates.ViewCoords);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setAdditiveSelection() {
        base.getSelectionManager().setSelectionMode(EditorSelectionManager.SelectionMode.Additive);
        screen.getFocusHandler().resetFocusElements();
    }

    public void setNormalSelection() {
        base.getSelectionManager().setSelectionMode(EditorSelectionManager.SelectionMode.Normal);
        screen.getFocusHandler().resetFocusElements();
    }

    public Element getRightPanel() {
        return rightPanel;
    }

    public Screen getScreen() {
        return screen;
    }

    public void newSceneButton() {
        if (!base.getEventManager().isActive()) {
            base.getSceneManager().newScene();

            // create new scene
            base.getSceneManager().createSceneObject("Scene1");
            base.getSceneManager().getScenesList().get("Scene1").createLayersGroup("LayersGroup1");

            clearGui();
            updateSceneGUI();

//            // set default layer1 (as it's set in the LayersGroup)
//            CheckBox cb = screen.findNiftyControl("layerVisibility1", CheckBox.class);
//            cb.check();
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void LoadSceneButton() {
        if (!base.getEventManager().isActive()) {
            boolean isLoaded = base.getSceneManager().loadScene();

            if (isLoaded == true) {
                clearGui();

                // reload assets lists
                int guiAssetLine = 1;

                // show assets at the gui
                assetsListBox.addAllItems(base.getSceneManager().getAssetsList());


                // update list of all entities
                ConcurrentHashMap<String, String> entList = base.getSceneManager().getEntitiesListsList();
                entitiesListBox.clear();
                for (String str : entList.keySet()) {
                    entitiesListBox.addItem(str);
                }


                updateSceneGUI();

                getEntitiesListBox().sortAllItems();
                getEntitiesListBox().refresh();
                getSceneObjectsListBox().sortAllItems();
                getSceneObjectsListBox().refresh();

            }
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void saveSceneButton() {
        if (!base.getEventManager().isActive()) {
            base.getEventManager().setAction(true);
            base.getSceneManager().saveScene();
            base.getEventManager().setAction(false);
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void saveAsNewSceneButton() {
        if (!base.getEventManager().isActive()) {
            base.getSceneManager().saveAsNewScene();
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public ListBox getEntitiesListBox() {
        return entitiesListBox;
    }

    public ListBox getSceneObjectsListBox() {
        return sceneObjectsListBox;
    }

    public void updateAssetsButton() {
        if (!base.getEventManager().isActive()) {
            // update assets
            base.getSceneManager().clearAssets();

            for (Object item : assetsListBox.getItems()) {
                String str = (String) item;

                if (str.length() > 0) {
                    base.getSceneManager().addAsset(str);
                }
            }

            // update list of all entities
            ConcurrentHashMap<String, String> entList = base.getSceneManager().getEntitiesListsList();
            entitiesListBox.clear();
            for (String str : entList.keySet()) {
                entitiesListBox.addItem(str);
            }
            entitiesListBox.sortAllItems();
            entitiesListBox.refresh();
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void addEntityToSceneButton() {
        // create entity
        if (entitiesListBox.getSelection().size() > 0 && base.getSceneManager().getActiveScene().getActivelayersGroup().getActiveLayer() != null
                && !base.getEventManager().isActive()) {
            String selectedEntity = entitiesListBox.getSelection().get(0).toString();
            long id = base.getSceneManager().createEntityModel(selectedEntity, base.getSceneManager().getEntitiesListsList().get(selectedEntity), null);
            Spatial entitySp = base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
            base.getSceneManager().getActiveScene().getActivelayersGroup().getActiveLayer().attachChild(entitySp);
            EditorTransformConstraint constraintTool = base.getTransformManager().getConstraintTool();

            if (constraintTool.getConstraint() > 0) {
                float x = constraintTool.constraintValue(entitySp.getLocalTranslation().getX());
                float y = constraintTool.constraintValue(entitySp.getLocalTranslation().getY());
                float z = constraintTool.constraintValue(entitySp.getLocalTranslation().getZ());
                entitySp.setLocalTranslation(new Vector3f(x, y, z));
            }


            // clear selection
            base.getSelectionManager().clearSelectionList();
            base.getSelectionManager().selectEntity(id, base.getSelectionManager().getSelectionMode());
            base.getSelectionManager().calculateSelectionCenter();

            // add entty to sceneList
            EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
            sceneObjectsListBox.addItem(nameComp.getName());
            sceneObjectsListBox.sortAllItems();
            sceneObjectsListBox.refresh();

            // set history
            base.getHistoryManager().prepareNewHistory();
            base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());
        }
        screen.getFocusHandler().resetFocusElements();
    }

    // This is just visual representation of selected objects
    protected void setSelectedObjectsList() {

        List<Long> selList = base.getSelectionManager().getSelectionList();

        for (Object indexDeselect : sceneObjectsListBox.getSelection()) {
            sceneObjectsListBox.deselectItem(indexDeselect);
        }

        for (Long id : selList) {
            EntityNameComponent nameComp = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
            String objectString = nameComp.getName();
            sceneObjectsListBox.selectItem(objectString);
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void removeClonesButton() {
        if (entitiesListBox.getSelection().size() > 0 && !base.getEventManager().isActive()) {
            base.getSceneManager().removeClones(entitiesListBox.getSelection().get(0).toString());
            base.getGuiManager().getSceneObjectsListBox().sortAllItems();
            base.getGuiManager().getSceneObjectsListBox().refresh();
        }
        screen.getFocusHandler().resetFocusElements();
    }

    // select entities from the list of seceneObjectsList
    public void selectEntitiesButton() {
//        List<Long> lectlList = base.getSelectionManager().getSelectionList();

        if (sceneObjectsListBox.getSelection().size() > 0) {

            base.getSelectionManager().clearSelectionList();
            for (Object obj : sceneObjectsListBox.getSelection()) {
                String objStr = (String) obj;
                long id = Long.valueOf(objStr.substring(objStr.indexOf("_IDX") + 4, objStr.length()));
                Node entNode = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
                System.out.println(objStr.substring(objStr.indexOf("_IDX") + 4, objStr.length()));

                // check if entity is in selected layer
                Node possibleLayer = (Node) entNode.getParent();
                Object isEnabledObj = possibleLayer.getUserData("isEnabled");
                boolean isEnabled = (Boolean) isEnabledObj;
                Object isLockedObj = possibleLayer.getUserData("isLocked");
                boolean isLocked = (Boolean) isLockedObj;

                if (isEnabled && !isLocked) {
                    base.getSelectionManager().selectEntity(id, EditorSelectionManager.SelectionMode.Additive);
                }
            }
            base.getSelectionManager().calculateSelectionCenter();
            setSelectedObjectsList();

            // set history
            base.getHistoryManager().prepareNewHistory();
            base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void showSelectedEntitiesButton() {
        setSelectedObjectsList();
        screen.getFocusHandler().resetFocusElements();
    }

    public void clearSelectedEntitiesButton() {
        for (Object indexDeselect : sceneObjectsListBox.getSelection()) {
            sceneObjectsListBox.deselectItem(indexDeselect);
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void removeSelectedButton() {
        for (long id : base.getSelectionManager().getSelectionList()) {
            EntityNameComponent nameComponent = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
            sceneObjectsListBox.removeItem(nameComponent.getName());

            base.getSceneManager().removeEntityObject(id);
        }

        base.getSelectionManager().getSelectionList().clear();
        base.getSelectionManager().calculateSelectionCenter();
        screen.getFocusHandler().resetFocusElements();
    }

    public void selectAllButton() {
        if (!base.getEventManager().isActive()) {

            if (base.getSelectionManager().getSelectionList().size() > 0) {
                base.getSelectionManager().clearSelectionList();
            } else {

                boolean isSceneEnabled = (Boolean) base.getSceneManager().getActiveScene().getSceneNode().getUserData("isEnabled");
                boolean isLayersGoupEnabled = (Boolean) base.getSceneManager().getActiveScene().getActivelayersGroup().getLayersGroupNode().getUserData("isEnabled");

                // if scene and layersGroup are enabled
                if (isLayersGoupEnabled && isSceneEnabled) {
                    for (Node spLayer : base.getSceneManager().getActiveScene().getActivelayersGroup().getLayers()) {

                        Node layerNode = (Node) spLayer;
                        boolean isLayerEnabled = (Boolean) layerNode.getUserData("isEnabled");
                        boolean isLayerLocked = (Boolean) layerNode.getUserData("isLocked");

                        // if layer is enabled - select entities
                        if (isLayerEnabled && !isLayerLocked) {
                            for (Spatial spEntity : layerNode.getChildren()) {
                                Node entityNode = (Node) spEntity;
                                long ID = (Long) entityNode.getUserData("EntityID");
                                base.getSelectionManager().selectEntity(ID, EditorSelectionManager.SelectionMode.Additive);
                            }
                        }
                    }
                }

            }

            base.getSelectionManager().calculateSelectionCenter();

            // set history
            base.getHistoryManager().prepareNewHistory();
            base.getHistoryManager().setNewSelectionHistory(base.getSelectionManager().getSelectionList());

        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void cloneSelectedButton() {
        if (base.getSelectionManager().getSelectionList().size() > 0) {
            List<Long> list = base.getSceneManager().cloneSelectedEntities();
            for (Long id : list) {
                EntityNameComponent newRealName = (EntityNameComponent) base.getEntityManager().getComponent(id, EntityNameComponent.class);
                base.getGuiManager().getSceneObjectsListBox().addItem(newRealName.getName());
            }
            list.clear();
            list = null;
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void addComponentButton() {
        // if entity is selected
        if (base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
            idComponentToChange = lastIdOfComponentList; // set emp id to change

            popupEditComponent.enable();
            nifty.showPopup(nifty.getCurrentScreen(), popupEditComponent.getId(), null);

            popupEditComponent.findNiftyControl("entityDataName", TextField.class).setText("");
            popupEditComponent.findNiftyControl("entityData", TextField.class).setText("");

            popupEditComponent.getFocusHandler().resetFocusElements();
            base.getEditorMappings().removeListener();

        }

    }

    public void removeSelectedComponentButton() {
        if (componentsListBox.getSelection().size() > 0
                && base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
            String strName = (String) componentsListBox.getSelection().get(0);
            base.getDataManager().getEntityData(lastIdOfComponentList).remove(strName);

            componentsListBox.removeItem(strName);
        }
        screen.getFocusHandler().resetFocusElements();

    }

    public void editComponent() {

        if (componentsListBox.getSelection().size() > 0) {
            // textFields
            String dataComponentName = (String) componentsListBox.getSelection().get(0);

            // if entity is selected
            if (base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
                idComponentToChange = lastIdOfComponentList; // set emp id to change

                popupEditComponent.enable();
                nifty.showPopup(nifty.getCurrentScreen(), popupEditComponent.getId(), null);

                ConcurrentHashMap<String, String> data = base.getDataManager().getEntityData(idComponentToChange);
                popupEditComponent.findNiftyControl("entityDataName", TextField.class).setText(dataComponentName);
                popupEditComponent.findNiftyControl("entityData", TextField.class).setText(data.get(dataComponentName));

                popupEditComponent.getFocusHandler().resetFocusElements();
                base.getEditorMappings().removeListener();
            }
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void copyComponentToSelectedEntityButton() {
        if (componentsListBox.getSelection().size() > 0
                && base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
            String strDataName = (String) componentsListBox.getSelection().get(0);
            String strData = base.getDataManager().getEntityData(lastIdOfComponentList).get(strDataName);
            List<Long> list = base.getSelectionManager().getSelectionList();
            for (long id : list) {
                if (id != lastIdOfComponentList) {
                    base.getDataManager().getEntityData(id).put(strDataName, strData);
                }
            }
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void removeComponentFromSelectedEntityButton() {
        if (componentsListBox.getSelection().size() > 0
                && base.getSelectionManager().getSelectionList().contains(lastIdOfComponentList)) {
            String strDataName = (String) componentsListBox.getSelection().get(0);
            List<Long> list = base.getSelectionManager().getSelectionList();
            for (long id : list) {
                if (id != lastIdOfComponentList
                        && base.getDataManager().getEntityData(id).containsKey(strDataName) == true) {
                    base.getDataManager().getEntityData(id).remove(strDataName);
                }
            }
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void finishEditComponent(String bool) {
        boolean boo = Boolean.valueOf(bool);
        // if entity is selected

        if (boo) {
            ConcurrentHashMap<String, String> data = base.getDataManager().getEntityData(idComponentToChange);

            String newDataName = popupEditComponent.findNiftyControl("entityDataName", TextField.class).getDisplayedText();
            String newData = popupEditComponent.findNiftyControl("entityData", TextField.class).getDisplayedText();
            data.put(newDataName, newData);

//            if (base.getSelectionManager().getSelectionList().size() > 0
//                    && base.getSelectionManager().getSelectionList().get(base.getSelectionManager().getSelectionList().size() - 1) == idComponentToChange) {
            if (componentsListBox.getItems().contains(newDataName) == false) {
                componentsListBox.addItem(newDataName);
            }
//            }
        }

        nifty.closePopup(popupEditComponent.getId());
        popupEditComponent.disable();
        popupEditComponent.getFocusHandler().resetFocusElements();
        screen.getFocusHandler().resetFocusElements();
        base.getEditorMappings().addListener();
    }

    public void finishEditAsset(String bool) {
        boolean boo = Boolean.valueOf(bool);
        // if entity is selected

        if (boo) {

            String newDataName = popupEditAsset.findNiftyControl("assetPathPopup", TextField.class).getDisplayedText();

            if (!assetsListBox.getSelection().isEmpty()) {
                assetsListBox.removeItem(assetsListBox.getSelection().get(0));
            }
            if (newDataName.length() > 0) {
                assetsListBox.addItem(newDataName);
            }
        }

        nifty.closePopup(popupEditAsset.getId());
        popupEditAsset.disable();
        popupEditAsset.getFocusHandler().resetFocusElements();
        screen.getFocusHandler().resetFocusElements();
        base.getEditorMappings().addListener();
    }

    public void AssetButton(String value) {
        if (value.equals("new")) {
            if (!assetsListBox.getSelection().isEmpty()) {
                assetsListBox.deselectItem(assetsListBox.getSelection().get(0));
            }

            screen.getFocusHandler().resetFocusElements();
            popupEditAsset.enable();
            nifty.showPopup(nifty.getCurrentScreen(), popupEditAsset.getId(), null);

            popupEditAsset.findNiftyControl("assetPathPopup", TextField.class).setText("");

            popupEditAsset.getFocusHandler().resetFocusElements();
            base.getEditorMappings().removeListener();
        } else if (value.equals("edit") && !assetsListBox.getSelection().isEmpty()) {
            popupEditAsset.enable();
            nifty.showPopup(nifty.getCurrentScreen(), popupEditAsset.getId(), null);

            popupEditAsset.findNiftyControl("assetPathPopup", TextField.class).setText(assetsListBox.getSelection().get(0).toString());

            popupEditAsset.getFocusHandler().resetFocusElements();
            base.getEditorMappings().removeListener();
        } else if (value.equals("remove") && !assetsListBox.getSelection().isEmpty()) {
            assetsListBox.removeItem(assetsListBox.getSelection().get(0));
            screen.getFocusHandler().resetFocusElements();
        }


    }

    // checkbox called
    public void savePreviewJ3O() {
        boolean savePreview = base.getSceneManager().getSavePreviewJ3o();
        if (savePreview) {
            base.getSceneManager().setSavePreviewJ3o(false);
            updateCheckbox(false, "savePreviewJ3O");
        } else {
            base.getSceneManager().setSavePreviewJ3o(true);
            updateCheckbox(true, "savePreviewJ3O");
        }
        screen.getFocusHandler().resetFocusElements();
    }

    public void lockLayer(String layerToLock) {
        if (!base.getEventManager().isActive()) {
            int iInt = Integer.valueOf(layerToLock);
            CheckBox cb = screen.findNiftyControl("layerLock" + layerToLock, CheckBox.class);
            Node layerToLockSP = base.getSceneManager().getActiveScene().getActivelayersGroup().getLayer(iInt); // layer to switch on/off

            Object isLockedObj = layerToLockSP.getUserData("isLocked");
            boolean isLocked = (Boolean) isLockedObj;

            if (isLocked) {
                cb.uncheck();
                layerToLockSP.setUserData("isLocked", false);
            } else {
                cb.check();
                layerToLockSP.setUserData("isLocked", true);

                checkSelectionList();

            }
        }
    }

    // This function is for removing of entities from the selectionList
    // if scene settings were changed
    private void checkSelectionList() {
        List<Long> selList = base.getSelectionManager().getSelectionList();
        List<Long> tempSelList = new ArrayList<Long>();

        for (Long id : selList) {
            Node selectedModel = (Node) base.getSpatialSystem().getSpatialControl(id).getGeneralNode();
            String isActiveSceneOfEntity = (String) selectedModel.getParent().getUserData("SceneName");
            String isActiveLayersGroupOfEntity = (String) selectedModel.getParent().getUserData("LayersGroupName");
            // if entity is on active scene and layersGroup - check entity's layer
            EditorLayersGroupObject activeLayerGroup = base.getSceneManager().getActiveScene().getActivelayersGroup();
            EditorSceneObject activeScene = base.getSceneManager().getActiveScene();

            if (activeScene.getSceneName().equals(isActiveSceneOfEntity)
                    && activeLayerGroup.getLayersGroupName().equals(isActiveLayersGroupOfEntity)) {

                boolean isActiveSceneEnabled = (Boolean) activeScene.getSceneNode().getUserData("isEnabled");
                boolean isActiveLayersGroupEnabled = (Boolean) activeScene.getSceneNode().getUserData("isEnabled");
                boolean isLayerOfEntityLocked = (Boolean) selectedModel.getParent().getUserData("isLocked");
                boolean isLayerOfEntityEnabled = (Boolean) selectedModel.getParent().getUserData("isEnabled");

                // remove entity from selection if scene or LayersGroup disabled and if layer is disabled or locked
                if (!isActiveSceneEnabled || isActiveLayersGroupEnabled
                        || isLayerOfEntityLocked || !isLayerOfEntityEnabled) {
                    tempSelList.add(id);
                }
            } // remove entity from selection if scene or layersGroup not active
            else {
                tempSelList.add(id);
            }

        }

        // do remove entities from selection
        for (Long idToRemove : tempSelList) {
            selList.remove(idToRemove);
            base.getSelectionManager().removeSelectionBox((Node) base.getSpatialSystem().getSpatialControl(idToRemove).getGeneralNode());
        }
        base.getSelectionManager().calculateSelectionCenter();
        tempSelList.clear();
        tempSelList = null;
    }

    public void switchLayer(String layerToShow) {
        if (!base.getEventManager().isActive()) {

            int iInt = Integer.valueOf(layerToShow);
            Node activeLayer = base.getSceneManager().getActiveScene().getActivelayersGroup().getActiveLayer(); // active layer
            Node layerToSwitch = base.getSceneManager().getActiveScene().getActivelayersGroup().getLayer(iInt); // layer to switch on/off
            Node activeLayersGroup = base.getSceneManager().getActiveScene().getActivelayersGroup().getLayersGroupNode();

            Object isEnabledObj = layerToSwitch.getUserData("isEnabled");
            boolean isEnabled = (Boolean) isEnabledObj;

            // Switching off
            if (isEnabled == true) {

                // detach layer
                activeLayersGroup.detachChild(layerToSwitch);
                layerToSwitch.setUserData("isEnabled", false);

                // remove layer from selection
                checkSelectionList();

                // if selected layer is active
                if (activeLayer.equals(layerToSwitch)) {
                    // deactivate active and slected layer
                    base.getSceneManager().getActiveScene().getActivelayersGroup().setActiveLayer(null);

                    // set new active layer
                    if (activeLayersGroup.getChildren().size() > 0) {
                        Node nd = (Node) activeLayersGroup.getChild(activeLayersGroup.getChildren().size() - 1);
                        base.getSceneManager().getActiveScene().getActivelayersGroup().setActiveLayer(nd);
                    } else {
                        base.getSceneManager().getActiveScene().getActivelayersGroup().setActiveLayer(null);
                    }
                }
            } // switching on
            else {
                //set active layer and enable it
                base.getSceneManager().getActiveScene().getActivelayersGroup().setActiveLayer(layerToSwitch);
                activeLayersGroup.attachChild(layerToSwitch);
                layerToSwitch.setUserData("isEnabled", true);
            }

            updateLayersGUI();
        }
    }

    public void moveToLayerEnable(String bool) {
        boolean boolValue = Boolean.valueOf(bool);
        if (boolValue) {
            screen.getFocusHandler().resetFocusElements();
            popupMoveToLayer.enable();
            nifty.showPopup(nifty.getCurrentScreen(), popupMoveToLayer.getId(), null);
            popupMoveToLayer.getFocusHandler().resetFocusElements();
            base.getEditorMappings().removeListener();
        } else {
            nifty.closePopup(popupMoveToLayer.getId());
            popupMoveToLayer.disable();
            popupMoveToLayer.getFocusHandler().resetFocusElements();
            base.getEditorMappings().addListener();
        }

    }

    /// function for nifty-listbox-my.xml GUI CHANGES.
    public void changeScene(String str) {
        if (!base.getEventManager().isActive()) {

            // set new active Scene
            if (str.equals("changeScene")) {
                List scenesList = scenesListbox.getSelection();
                if (scenesList.size() > 0) {
                    String sceneName = (String) scenesList.get(0);
                    base.getSceneManager().setActiveScene(base.getSceneManager().getScenesList().get(sceneName));
                    checkSelectionList();
                }
            } // set new active layersGroup
            else if (str.equals("changeLayersGroup")) {
                List layersGroupsList = layersGroupsListbox.getSelection();
                if (layersGroupsList.size() > 0) {
                    String sceneName = (String) layersGroupsList.get(0);
                    base.getSceneManager().getActiveScene().setActivelayersGroup(base.getSceneManager().getActiveScene().getLayerGroupsList().get(sceneName));
                    checkSelectionList();
                }
            }

            updateSceneGUI();
            System.out.println("SceneGUI Updated!");
        }
    }

    public void enableSceneCheckbox(String str) {
        if (!base.getEventManager().isActive()) {

            // Scene
            if (str.equals("isSceneEnabled")) {
                EditorSceneObject activeScene = base.getSceneManager().getActiveScene();
                boolean sceneIsEnabled = (Boolean) activeScene.getSceneNode().getUserData("isEnabled");
                if (sceneIsEnabled) {
                    activeScene.setSceneEnabled(false);
                    updateCheckbox(false, "isSceneEnabled");
                } else {
                    activeScene.setSceneEnabled(true);
                    updateCheckbox(true, "isSceneEnabled");
                }
            } // LayersGroup
            else if (str.equals("isLayersGroupEnabled")) {
                EditorLayersGroupObject activelayersGroup = base.getSceneManager().getActiveScene().getActivelayersGroup();
                boolean sceneIsEnabled = (Boolean) activelayersGroup.getLayersGroupNode().getUserData("isEnabled");
                if (sceneIsEnabled) {
                    activelayersGroup.setLayersGroupEnabled(false);
                    updateCheckbox(false, "isLayersGroupEnabled");
                } else {
                    activelayersGroup.setLayersGroupEnabled(true);
                    updateCheckbox(true, "isLayersGroupEnabled");
                }
            }

            checkSelectionList(); // update selection
            screen.getFocusHandler().resetFocusElements();
        }
    }

    // This is a temporary fix for updating checkboxes for tabs 
    // this function is used in the Interface/Styles/nifty-tabs-my.xml
    public void updateTabs() {
        if (!base.getEventManager().isActive()) {

            TabGroup tabGroup = screen.findNiftyControl("settingsTabsGroup", TabGroup.class);
            String selectedTabID = tabGroup.getSelectedTab().getId();
            updateCheckBoxes(tabGroup.getSelectedTab().getElement());

            // fix for selected layer red color
            if (selectedTabID.equals("SceneTab")) {
                updateLayersGUI();
            }

            screen.getFocusHandler().resetFocusElements();
            System.out.println("Checkboxes are updated!");

        }

    }

    private void updateCheckBoxes(Element mainElement) {
        for (Element childElement : mainElement.getElements()) {

            if (!childElement.getNiftyControl(CheckBox.class).getClass().equals(CheckBoxNull.class)) {
                // partial fix for checkboxes
                CheckBox cb = childElement.getNiftyControl(CheckBox.class);
                cb.getElement().resetAllEffects();
                if (cb.isChecked()) {
                    cb.uncheck();
                    cb.check();
                }
                System.out.println("Found!" + cb);
            }

            // recourse function
            if (childElement.getElements().size() > 0) {
                updateCheckBoxes(childElement);
            }
        }
    }

    public void moveToLayer(String srtinG) {
        // move to layer
        int iInt = Integer.valueOf(srtinG);
        List<Long> lst = base.getSelectionManager().getSelectionList();
        for (Long lng : lst) {
            Node moveNode = (Node) base.getSpatialSystem().getSpatialControl(lng).getGeneralNode();
            base.getSceneManager().getActiveScene().getActivelayersGroup().addToLayer(moveNode, iInt);
        }

        // clear selection if layer is inactive
        Object isEnabledObj = base.getSceneManager().getActiveScene().getActivelayersGroup().getLayer(iInt).getUserData("isEnabled");
        boolean isEnabled = (Boolean) isEnabledObj;
        Object isLockedObj = base.getSceneManager().getActiveScene().getActivelayersGroup().getLayer(iInt).getUserData("isLocked");
        boolean isLocked = (Boolean) isLockedObj;

        // remove from selection
        if (!isEnabled || isLocked) {
            base.getSelectionManager().clearSelectionList();
            base.getSelectionManager().calculateSelectionCenter();
        }

        nifty.closePopup(popupMoveToLayer.getId());
        popupMoveToLayer.disable();
        screen.getFocusHandler().resetFocusElements();
        base.getEditorMappings().addListener();

    }

    private void createGrid() {
        gridNode = new Node("gridNode");

        //Create a grid plane
        Geometry g = new Geometry("GRID", new Grid(201, 201, 10f));
        Material floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floor_mat.getAdditionalRenderState().setWireframe(true);
        floor_mat.setColor("Color", new ColorRGBA(0.4f, 0.4f, 0.4f, 0.15f));
        floor_mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        g.setCullHint(Spatial.CullHint.Never);
        g.setShadowMode(RenderQueue.ShadowMode.Off);
        g.setQueueBucket(RenderQueue.Bucket.Transparent);
        g.setMaterial(floor_mat);
        g.center().move(new Vector3f(0f, 0f, 0f));
        gridNode.attachChild(g);

        // Red line for X axis
        final Line xAxis = new Line(new Vector3f(-1000f, 0f, 0f), new Vector3f(1000f, 0f, 0f));
        xAxis.setLineWidth(2f);
        Geometry gxAxis = new Geometry("XAxis", xAxis);
        gxAxis.setModelBound(new BoundingBox());
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(1.0f, 0.2f, 0.5f, 0.2f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        gxAxis.setCullHint(Spatial.CullHint.Never);
        gxAxis.setQueueBucket(RenderQueue.Bucket.Transparent);
        gxAxis.setShadowMode(RenderQueue.ShadowMode.Off);
        gxAxis.setMaterial(mat);
        gxAxis.setCullHint(Spatial.CullHint.Never);

        gridNode.attachChild(gxAxis);

        // Blue line for Z axis
        final Line zAxis = new Line(new Vector3f(0f, 0f, -1000f), new Vector3f(0f, 0f, 1000f));
        zAxis.setLineWidth(2f);
        Geometry gzAxis = new Geometry("ZAxis", zAxis);
        gzAxis.setModelBound(new BoundingBox());
        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", new ColorRGBA(0.2f, 1.0f, 0.2f, 0.2f));
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        gzAxis.setCullHint(Spatial.CullHint.Never);
        gzAxis.setQueueBucket(RenderQueue.Bucket.Transparent);
        gzAxis.setShadowMode(RenderQueue.ShadowMode.Off);
        gzAxis.setMaterial(mat);
        gzAxis.setCullHint(Spatial.CullHint.Never);
        gridNode.attachChild(gzAxis);

        rootNode.attachChild(gridNode);

    }

    public Node getGridNode() {
        return gridNode;
    }

//    private void createSimpleGui() {
//
//        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
//        BitmapText ch = new BitmapText(guiFont, false);
//        ch.setSize(guiFont.getCharSet().getRenderedSize());
//        ch.setText("W,A,S,D,Q,Z, MiddleMouseButton, RightMouseButton, Scroll"); // crosshairs
//        ch.setColor(new ColorRGBA(1f, 0.8f, 0.1f, 0.3f));
//        ch.setLocalTranslation(application.getCamera().getWidth() * 0.1f, application.getCamera().getHeight() * 0.1f, 0);
//        guiNode.attachChild(ch);
//
//    }
    @Override
    public void update(float tpf) {
        // This is for componentsList!
        List<Long> selList = base.getSelectionManager().getSelectionList();
        if (selList.size() == 0) {
            componentsListBox.clear();
            lastIdOfComponentList = -1; // just for the case if user will select the same entity
        } else if (selList.get(selList.size() - 1) != lastIdOfComponentList) {
            componentsListBox.clear();
            lastIdOfComponentList = selList.get(selList.size() - 1);
            ConcurrentHashMap<String, String> data = base.getDataManager().getEntityData(lastIdOfComponentList);
            for (String key : data.keySet()) {
                componentsListBox.addItem(key);
            }
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        //TODO: clean up what you initialized in the initialize method,
        //e.g. remove all spatials from rootNode
        //this is called on the OpenGL thread after the AppState has been detached
    }

    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
}