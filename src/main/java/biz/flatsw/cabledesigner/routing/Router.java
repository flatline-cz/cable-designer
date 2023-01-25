/*******************************************************************************
 * Copyright (c) 2022. originally created by flatline.cz
 * This file is a part of CABLE-DESIGNER project.
 *
 *  This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************************/

package biz.flatsw.cabledesigner.routing;

import biz.flatsw.cabledesigner.Services;
import biz.flatsw.cabledesigner.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class Router {
    private final Map<String, List<Pin>> terminals = new TreeMap<>();

    // actual routing context
    private Map<String, RouteInfo> routeInfoMap = new TreeMap<>();


    public Router() {
    }

    private int routeConnector(Connector startNode) {
        // has been reached?
        RouteInfo nodeInfo = routeInfoMap.get(startNode.getName());
        if (nodeInfo == null)
            return 0;

        // get opposite node name
        Cable cable = startNode.getCable();
        String oppositeName = cable.getOppositeNode(startNode).getName();
        int length = nodeInfo.distance + cable.getLength();

        // opposite node already routed better?
        RouteInfo oppositeInfo = routeInfoMap.get(oppositeName);
        if (oppositeInfo != null && oppositeInfo.distance <= length)
            return 0;

        // update routing info
        List<String> path = new ArrayList<>(nodeInfo.path);
        path.add(oppositeName);
        oppositeInfo = new RouteInfo(path, length);
        routeInfoMap.put(oppositeName, oppositeInfo);
        return 1;
    }

    private int routeJunction(Junction startNode) {
        // has been reached?
        RouteInfo nodeInfo = routeInfoMap.get(startNode.getName());
        if (nodeInfo == null)
            return 0;

        int count = 0;

        for (Cable cable : startNode.getCables()) {
            // get opposite node name
            String oppositeName = cable.getOppositeNode(startNode).getName();
            int length = nodeInfo.distance + cable.getLength();

            // opposite node already routed better?
            RouteInfo oppositeInfo = routeInfoMap.get(oppositeName);
            if (oppositeInfo != null && oppositeInfo.distance <= length)
                continue;

            // update routing info
            List<String> path = new ArrayList<>(nodeInfo.path);
            path.add(oppositeName);
            oppositeInfo = new RouteInfo(path, length);
            routeInfoMap.put(oppositeName, oppositeInfo);
            count++;
        }
        return count;
    }

    private int routeNode(PathNode startNode) {
        if (startNode instanceof Connector)
            return routeConnector((Connector) startNode);
        if (startNode instanceof Junction)
            return routeJunction((Junction) startNode);
        return 0;
    }

    private ArrayList<RouteNode> routeAny(String startConnName, final Set<String> targets) {
        // initialize map
        routeInfoMap.clear();
        routeInfoMap.put(startConnName, new RouteInfo(startConnName));

        // get a list of all graph nodes
        List<PathNode> graphNodes = Services.getPathManager().listNodes();

        // actual routing
        while (true) {
            int numberOfUpdatedNodes = 0;
            for (PathNode graphNode : graphNodes) {
                numberOfUpdatedNodes += routeNode(graphNode);
            }
            if (numberOfUpdatedNodes == 0)
                break;
        }

        // shortest path
        RouteInfo path = routeInfoMap
                .values()
                .stream()
                .filter(info -> info.endsWithTarget(targets))
                .sorted()
                .findFirst()
                .orElse(null);
        if (path == null)
            return new ArrayList<>();

        // create path
        ArrayList<RouteNode> list = new ArrayList<>();
        for (int i = 0; i < path.path.size(); i++) {
            if (i != 0) {
                // get cable
                list.add(new RouteNodeWire(
                        Services.getCableManager()
                                .findByEnds(
                                        path.path.get(i - 1),
                                        path.path.get(i))));
            }
            // add connector
            String terminalName = path.path.get(i);
            list.add(new RouteNodeTerminal(
                    terminalName.startsWith("C")
                            ? Services.getConnectorManager()
                            .getConnector(terminalName)
                            : Services.getPathManager()
                            .getJunction(terminalName)));
        }
        return list;
    }

    public void route(Signal signal) {
        System.out.println("Routing signal: " + signal);

        for (SignalPath signalPath : signal.getSignalPaths()) {
            route(signalPath);
        }
    }

    public void route(SignalPath signalPath) {
        System.out.println("Routing signal path");

        // get a list of connectors
        List<String> connNameList = new ArrayList<>();
        for (Pin pin : signalPath.listTerminals()) {
            String connName = pin.getConnector().getName();
            if(!connNameList.contains(connName))
                connNameList.add(connName);
            List<Pin> conn = terminals.computeIfAbsent(connName, k -> new ArrayList<>());
            conn.add(pin);
        }
        System.out.println("- connectors: " + connNameList);

        if (connNameList.size() < 2)
            throw new RuntimeException("Invalid signal '" + signalPath.getSignal() + "', unable to route");

        List<RouteNode> route = null;

        // ordered?
        if (signalPath.isOrdered()) {
            String startConnName = connNameList.get(0);
            for (int targetIdx = 1; targetIdx < connNameList.size(); targetIdx++) {
                Set<String> targets = new TreeSet<>();
                String targetConnName = connNameList.get(targetIdx);
                targets.add(targetConnName);
                if (route == null)
                    route = routeAny(startConnName, targets);
                else
                    route.addAll(routeAny(startConnName, targets));
                updateTargets(targets, route);
                startConnName = targetConnName;
            }

        } else {
            // route 1st 2 connectors
            Set<String> targets = new TreeSet<>(connNameList);
            String connName = connNameList.get(0);
            targets.remove(connName);
            route = routeAny(connName, targets);
            updateTargets(targets, route);

            // route all other connectors
            while (!targets.isEmpty()) {
                List<RouteNode> routeFromStart = routeAny(
                        ((RouteNodeTerminal) route.get(0)).getNode().getName(),
                        targets);
                List<RouteNode> routeFromEnd = routeAny(
                        ((RouteNodeTerminal) route.get(route.size() - 1)).getNode().getName(),
                        targets);
                if (countLength(routeFromStart) < countLength(routeFromEnd)) {
                    // prepend route
                    List<RouteNode> list = new ArrayList<>();
                    for (int i = routeFromStart.size() - 1; i > 0; i--)
                        list.add(routeFromStart.get(i));
                    list.addAll(route);
                    route = list;
                } else {
                    // append route
                    for (int i = 1; i < routeFromEnd.size(); i++)
                        route.add(routeFromEnd.get(i));
                }
                updateTargets(targets, route);
            }
        }
        System.out.println(dumpRoute(route));

        SignalWiring signalWiring=Services
                .getSignalManager()
                .createSignalWiring(signalPath.getSignal());

        // create final route (map connector pins)
        WireChain wireChain = null;
        List<Cable> cables = new ArrayList<>();
        Connector lastConnector = null;
        for (RouteNode routeNode : route) {
            // terminal?
            if (routeNode instanceof RouteNodeTerminal) {
                RouteNodeTerminal terminalNode = (RouteNodeTerminal) routeNode;
                PathNode pathNode = terminalNode.getNode();
                if (pathNode instanceof Connector) {
                    Connector connectorNode = (Connector) pathNode;
                    Pin previousPin = null;
                    for (Pin pin : connectorNode.findPinsBySignalPath(signalPath)) {
                        // first pin at all?
                        if (wireChain == null) {
                            wireChain = signalWiring.createWireChain(signalPath, pin);
                        } else {
                            // not a first pin in the connector?
                            if (previousPin != null) {
                                // add wire
                                wireChain.addInterPinConnection(previousPin, pin);
                            } else {
                                // first pin
                                wireChain.addInterConnectorWire(lastConnector, cables);
                                cables.clear();
                            }
                            wireChain.addPin(pin);

                        }
                        previousPin = pin;
                    }
                    lastConnector = connectorNode;
                    continue;
                }
            }
            if (routeNode instanceof RouteNodeWire) {
                RouteNodeWire wireNode = (RouteNodeWire) routeNode;
                cables.add(wireNode.getCable());
            }
        }
    }

    private String dumpRoute(List<RouteNode> route) {
        return route
                .stream()
                .map(RouteNode::toString)
                .collect(Collectors.joining(" - "));
    }

    private int countLength(List<RouteNode> nodes) {
        int length = 0;
        for (RouteNode node : nodes)
            if (node instanceof RouteNodeWire)
                length += ((RouteNodeWire) node).getCable().getLength();
        return length;
    }

    private void updateTargets(Set<String> targets, List<RouteNode> route) {
        RouteNode node = route.get(0);
        if (node instanceof RouteNodeTerminal)
            targets.remove(((RouteNodeTerminal) node).getNode().getName());
        node = route.get(route.size() - 1);
        if (node instanceof RouteNodeTerminal)
            targets.remove(((RouteNodeTerminal) node).getNode().getName());
//        System.err.println("Targets: "+targets);
//        dumpRoute(route);
    }

    private static class RouteInfo implements Comparable<RouteInfo> {
        private List<String> path;
        private int distance;

        public RouteInfo(List<String> path, int distance) {
            this.path = path;
            this.distance = distance;
        }

        public RouteInfo(String pathStartNode) {
            this.path = new ArrayList<>();
            this.path.add(pathStartNode);
            distance = 0;
        }

        public boolean endsWithTarget(Set<String> targets) {
            if (path.size() < 2)
                return false;
            return targets.contains(path.get(path.size() - 1));
        }


        @Override
        public int compareTo(RouteInfo o) {
            return Integer.compare(distance, o.distance);
        }

        @Override
        public String toString() {
            return path.toString() + " (" + distance + ")\n";
        }
    }


}
