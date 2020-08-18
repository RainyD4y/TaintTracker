package cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.ExceptionalUnitGraph;


public class CFG {

    public SootMethod method;

    private ExceptionalUnitGraph unitGraph = null;
    private Map<Unit, Node> allNodes = new HashMap<>();

    private static ConcurrentHashMap<SootMethod, CFG> allCFG = new ConcurrentHashMap<>();

    public static CFG getCFG(SootMethod sootMethod) {
        try {
            if (allCFG.keySet().contains(sootMethod))
                return allCFG.get(sootMethod);
            CFG cfg = new CFG(sootMethod);
            allCFG.put(sootMethod, cfg);
            return cfg;
        } catch (Exception e) {
            System.err.println("Can not get CFG for : " +
                    (sootMethod == null ? "NULL Method" : sootMethod.getSignature()));
        }
        return null;
    }

    private CFG(SootMethod sootMethod) {
        this.method = sootMethod;
        Body body = sootMethod.retrieveActiveBody();
        if (body != null) {
            unitGraph = new ExceptionalUnitGraph(body);
            for (Unit unit : unitGraph.getBody().getUnits()) {
                allNodes.put(unit, new Node(unit));
            }
            for (Unit unit : unitGraph.getBody().getUnits()) {
                Node node = getNodeByUnit(unit);
                for (Unit preUnit : unitGraph.getPredsOf(unit)) {
                    Node preNode = getNodeByUnit(preUnit);
                    if (!node.precursorNodes.contains(preNode))
                        node.precursorNodes.add(preNode);
                    if (!preNode.successorNodes.contains(node))
                        preNode.successorNodes.add(node);
                }
            }
        }
    }


    public Set<String> getSignaturesOfDirectCallee() {
        HashSet<String> calleeSigs = new HashSet<>();
        if (method.isConcrete()) {
            Body body = method.retrieveActiveBody();
            for (Unit unit : body.getUnits()) {
                if (((Stmt) unit).containsInvokeExpr()) {
                    calleeSigs.add(((Stmt) unit).getInvokeExpr().getMethod().getSignature());
                }
            }
        }
        return calleeSigs;
    }


    public List<Path> getAllPaths() {
        List<Path> result = new ArrayList<>();
        if (unitGraph != null) {
            Unit header = unitGraph.getHeads().get(0);
            return getPathsFromUnit(header, new HashSet<>());
        }
        return result;
    }

    public List<Path> getBackwardPathsFromUnit(Unit unit) {
        List<Path> result = new ArrayList<>();
        Node node = getNodeByUnit(unit);
        if (node == null) {
            return result;
        }
        if(node.precursorNodes.isEmpty()) {
            Path path = new Path();
            path.nodes.add(node);
            result.add(path);
        } else {
            List<Path> prePaths = new ArrayList<>();
            for (Node prenode : node.precursorNodes) {
//                Set<Node> subHistory = new HashSet<>();
//                subHistory.addAll(history);
                prePaths.addAll(getBackwardPathsFromUnit(prenode.unit));
            }
            for (Path path : prePaths) {
                path.addHead(node);
                result.add(path);
            }
        }
        return result;
    }

    public List<Path> getPathsFromUnit(Unit unit, Set<Node> history) {
        List<Path> result = new ArrayList<>();
        Node node = getNodeByUnit(unit);
        if (node == null || history.contains(node))
            return result;
        history.add(node);
        if (node.successorNodes.isEmpty()) {
            Path path = new Path();
            path.nodes.add(node);
            result.add(path);
        } else {
            List<Path> successorPaths = new ArrayList<>();
            for (Node succNode : node.successorNodes) {
                Set<Node> subHistory = new HashSet<>();
                subHistory.addAll(history);
                successorPaths.addAll(getPathsFromUnit(succNode.unit, subHistory));
            }
            for (Path path : successorPaths) {
                path.addHead(node);
                result.add(path);
            }
        }
        return result;
    }

    public Node getNodeByUnit(Unit unit) {
        return allNodes.get(unit);
    }
}

