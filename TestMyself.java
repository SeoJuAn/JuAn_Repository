package TestMyself;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.yworks.yfiles.geometry.InsetsD;
import com.yworks.yfiles.geometry.PointD;
import com.yworks.yfiles.graph.FilteredGraphWrapper;
import com.yworks.yfiles.graph.GraphItemTypes;
import com.yworks.yfiles.graph.IEdge;
import com.yworks.yfiles.graph.IGraph;
import com.yworks.yfiles.graph.ILabel;
import com.yworks.yfiles.graph.ILabelOwner;
import com.yworks.yfiles.graph.IModelItem;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.IPort;
import com.yworks.yfiles.graph.labelmodels.EdgeSegmentLabelModel;
import com.yworks.yfiles.graph.labelmodels.EdgeSides;
import com.yworks.yfiles.graph.labelmodels.ExteriorLabelModel;
import com.yworks.yfiles.graph.labelmodels.ILabelModelParameter;
import com.yworks.yfiles.graph.styles.ShapeNodeShape;
import com.yworks.yfiles.graph.styles.ShapeNodeStyle;
import com.yworks.yfiles.layout.hierarchic.HierarchicLayout;
import com.yworks.yfiles.layout.organic.OrganicLayout;
import com.yworks.yfiles.layout.radial.RadialLayout;
import com.yworks.yfiles.layout.tree.BalloonLayout;
import com.yworks.yfiles.layout.tree.ClassicTreeLayout;
import com.yworks.yfiles.view.Colors;
import com.yworks.yfiles.view.GraphComponent;
import com.yworks.yfiles.view.GraphOverviewComponent;
import com.yworks.yfiles.view.input.CommandAction;
import com.yworks.yfiles.view.input.GraphEditorInputMode;
import com.yworks.yfiles.view.input.GraphViewerInputMode;
import com.yworks.yfiles.view.input.ICommand;



public class TestMyself {
	
	GraphComponent graphcomponent; 
	JFrame frame;
	JToolBar toolbar = new JToolBar();
	INode [] nodes;
	IEdge [] edges;
	static String [][] data_for_node;
	static String [][] data_for_edge;
	int current_node=0;
	
	
	static private JButton filterButton;
	static private JButton resetButton_for_edge;
	static private JButton resetButton_for_node;
	JTextField text1;
	JTextField text2;
	JTextField text3;
	
	//tooltips와 addItemClickedListener을 동시에 적용하기 위해서 전역변수로 둬야함.
	GraphEditorInputMode mode = new GraphEditorInputMode();
	
	public TestMyself() {
		graphcomponent = new GraphComponent();
		graphcomponent.fitGraphBounds();
		
	}
	
	public void Create() {
		DesignNode();
		
		CreateGraph();
		
		SetInputMode();
		
		SetFileIOEnabled();
		
		Undo();
		
		GraphEditorInputMode geim = initializeInputMode();
		initializeTooltips(geim);
		
		CreateToolbar();
		
		SetFilteredGraph();
		
		CreateFrame();
	}
	

	
	public JComboBox LayoutCombobox() {
		JComboBox comboBox = new JComboBox();
		String [] layout = new String[] {"null","Hierarchic", "Organic", "Balloon", "Tree","Radial"};
		DefaultComboBoxModel comboModel = new DefaultComboBoxModel(layout);
		comboBox.setModel(comboModel);
		comboBox.setMaximumSize(comboBox.getPreferredSize());
		comboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String ChoicedLayout = comboBox.getSelectedItem().toString();
				if(ChoicedLayout == "Hierarchic") {
					HierarchicLayout layout = new HierarchicLayout();
					//layout.setNodeSizeConsiderationEnabled(true);
					//layout.setMinimumNodeDistance(50);
					graphcomponent.morphLayout(layout, Duration.ofMillis(500));
				}
				if(ChoicedLayout == "Organic") {
					OrganicLayout layout = new OrganicLayout();
					layout.setNodeSizeConsiderationEnabled(true);
					layout.setMinimumNodeDistance(50);
					graphcomponent.morphLayout(layout, Duration.ofMillis(500));
				}
				if(ChoicedLayout == "Balloon") {
					BalloonLayout layout = new BalloonLayout();
					graphcomponent.morphLayout(layout, Duration.ofMillis(500));
				}
				if(ChoicedLayout == "Tree") {
					ClassicTreeLayout layout = new ClassicTreeLayout();
					graphcomponent.morphLayout(layout, Duration.ofMillis(500));
				}
				if(ChoicedLayout == "RadialLayout") {
					RadialLayout layout = new RadialLayout();
					graphcomponent.morphLayout(layout, Duration.ofMillis(500));
				}
			}
			
		});
		return comboBox;
	}
	private void initializeTooltips(GraphEditorInputMode geim) {
	    // register a listener that sets a tooltip for a hovered item
	    geim.addQueryItemToolTipListener((source, args) -> {
	      if (args.isHandled()) {
	        // a tooltip has already been assigned -> nothing to do
	        return;
	      }

	      // creates a text for the tooltips
	      String title = "";
	      if (args.getItem() instanceof INode) {
	    	  int node_index = 0;
	    	  for(int i=0 ; i<data_for_node.length;i++) {
	    		  if(args.getItem().toString().substring(7).equals(data_for_node[i][2])) {
	    			  node_index = i;
	    			  break;
	    		  }
	    	  }
	        title = "계좌 : "+data_for_node[node_index][2]+" 은행 : "+data_for_node[node_index][1]+" 소유주 : "+data_for_node[node_index][0];
	        
	      } else if (args.getItem() instanceof IEdge) {
	    	  int edge_index = 0;
	    	  for(int i=0 ; i<data_for_edge.length;i++) {
	    		  if(args.getItem().toString().equals(data_for_edge[i][7])) {
	    			  edge_index = i;
	    			  break;
	    		  }
	    	  }
	        title = "금액 : "+data_for_edge[edge_index][6]+"일시 : "+data_for_edge[edge_index][8];
	      } 
	      /*else if (args.getItem() instanceof IPort) {
	        title = "Port Tooltip";
	      }*/
	      /*else if (args.getItem() instanceof ILabel) {
	        title = "라벨";
	      }*/


	      // extends the text with label information if available
	      String label = "";
	      IModelItem item = args.getItem(); 
	      if (item instanceof INode || item instanceof IEdge || item instanceof IPort) {
	        if (((ILabelOwner) item).getLabels().size() > 0) {
	          label = ((ILabelOwner) item).getLabels().first().getText();
	        }
	      } else if (item instanceof ILabel) {
	        label = ((ILabel) item).getText();
	      }

	      // use some HTML to format the tooltip
	      args.setToolTip("<html>" +
	          "<p><b>" + title + "</b></p>" +
	          //"<p>" + label + "</p>" +
	          "</html>");

	      // indicate that the tooltip has been set
	      args.setHandled(true);
	    });
	  }

	
	  /**
	   * Loads an initial sample graph.
	   */

	  /**
	   * Initializes the input mode for the demo.
	   */
	  private GraphEditorInputMode initializeInputMode() {
	    //GraphEditorInputMode geim = new GraphEditorInputMode();
	    mode.setGroupingOperationsAllowed(true);
	    graphcomponent.setInputMode(mode);
	    return mode;
	  }
	/*public AbstractAction OrganicLayout(){
		AbstractAction layoutaction = new AbstractAction("OrganicLayout") {

			@Override
			public void actionPerformed(ActionEvent e) {
				OrganicLayout layout = new OrganicLayout();
				layout.setNodeSizeConsiderationEnabled(true);
				layout.setMinimumNodeDistance(50);
				graphcomponent.morphLayout(layout, Duration.ofMillis(500));
			}
			
		};
		
		return layoutaction;
		
	}*/
	/*public AbstractAction HierarchicLayout(){
		AbstractAction layoutaction = new AbstractAction("HierarchicLayout") {

			@Override
			public void actionPerformed(ActionEvent e) {
				HierarchicLayout layout = new HierarchicLayout();
				//layout.setNodeSizeConsiderationEnabled(true);
				//layout.setMinimumNodeDistance(50);
				graphcomponent.morphLayout(layout, Duration.ofMillis(500));
			}
			
		};
		
		return layoutaction;
		
	}
	 */
	/*
	public AbstractAction TreeLayout(){
		AbstractAction layoutaction = new AbstractAction("TreeLayout") {

			@Override
			public void actionPerformed(ActionEvent e) {
				TreeLayout layout = new TreeLayout();
				//layout.setNodeSizeConsiderationEnabled(true);
				//layout.setMinimumNodeDistance(50);
				graphcomponent.morphLayout(layout, Duration.ofMillis(500));
			}
			
		};
		
		return layoutaction;
		
	}
	*/
	public void Undo() {
		graphcomponent.getGraph().setUndoEngineEnabled(true);
	}
	
	
	public void SetFileIOEnabled() {
		graphcomponent.setFileIOEnabled(true);
	}
	
	public void SetInputMode() {
		graphcomponent.setInputMode(new GraphEditorInputMode());
	}
	
	//Create Toolbar 
	public void CreateToolbar() {
		//JToolBar toolbar = new JToolBar();
		toolbar.add(new CommandAction(ICommand.OPEN,null,graphcomponent));
		toolbar.add(new CommandAction(ICommand.SAVE,null,graphcomponent));
		toolbar.addSeparator();
		toolbar.add(new CommandAction(ICommand.UNDO,null,graphcomponent));
		toolbar.add(new CommandAction(ICommand.REDO,null,graphcomponent));
		
		toolbar.addSeparator();
		toolbar.add(new JLabel("Layout : "));
		toolbar.add(LayoutCombobox());
		toolbar.addSeparator();
		
		toolbar.add(new JLabel("기간 : "));
		text1 = new JTextField(10);
		text2 = new JTextField(10);
		text1.setMaximumSize(text1.getPreferredSize());
		text2.setMaximumSize(text2.getPreferredSize());
		toolbar.add(text1);
		toolbar.add(new JLabel("~"));
		toolbar.add(text2);
		toolbar.add(filterButton = createTextButton("Filter selected items", "기간조회", actionEvent -> filterEdges()));
		toolbar.add(resetButton_for_edge = createTextButton("Resets the filter state of all items", "기간 전체조회", actionEvent -> resetFilter()));
		
		toolbar.addSeparator();
		toolbar.add(new JLabel("검색어 : "));
		text3 = new JTextField(10);
		text3.setMaximumSize(text3.getPreferredSize());
		toolbar.add(text3);
		toolbar.add(createTextButton("Filter selected items", "검색", actionEvent -> filterNodes()));
		toolbar.add(resetButton_for_node = createTextButton("Resets the filter state of all items", "검색어 삭제", actionEvent -> resetFilter()));
		//toolbar.add(OrganicLayout());
		//toolbar.add(HierarchicLayout());
		//toolbar.add(TreeLayout());
		//frame.add(toolbar,BorderLayout.NORTH);
	}
	
	public void SetFilteredGraph() {
		IGraph graph = graphcomponent.getGraph();
		FilteredGraphWrapper filteredGraph = createFilterGraph(graph);
		graphcomponent.setGraph(filteredGraph);
	}
	
	
	private FilteredGraphWrapper createFilterGraph(IGraph fullGraph) {
	    // hide items whose tag contains the string 'filtered'
	    Predicate<INode> nodePredicate = node -> !"filtered".equals(node.getTag());
	    Predicate<IEdge> edgePredicate = edge -> !"filtered".equals(edge.getTag());

	    // creates the filtered graph
	    FilteredGraphWrapper filteredGraph = new FilteredGraphWrapper(fullGraph, nodePredicate, edgePredicate);

	    return filteredGraph;
	  }
	public void filterNodes() {
	    // marks the selected items such that the nodePredicate or edgePredicate will filter them
	    //graphcomponent.getSelection().getSelectedNodes().forEach(node -> node.setTag("filtered"));
	    //graphcomponent.getSelection().getSelectedEdges().forEach(edge -> edge.setTag("filtered"));
		String search = text3.getText();
		
	    for(int i =0 ; i<data_for_edge.length;i++) {
	    	if(!((search.equals(data_for_edge[i][0])) || (search.equals(data_for_edge[i][2]))
	    			|| (search.equals(data_for_edge[i][3]))|| (search.equals(data_for_edge[i][5])))) {
	    		edges[i].setTag("filtered");
	    	}
	    }
	    
	    // re-evaluate the filter predicates to actually hide the items
	    FilteredGraphWrapper filteredGraph = (FilteredGraphWrapper) graphcomponent.getGraph();
	    filteredGraph.nodePredicateChanged();
	    filteredGraph.edgePredicateChanged();
 
	    // enable the reset buttons
	    resetButton_for_node.setEnabled(true);
	  }
	
	public void filterEdges() {
	    // marks the selected items such that the nodePredicate or edgePredicate will filter them
	    //graphcomponent.getSelection().getSelectedNodes().forEach(node -> node.setTag("filtered"));
	    //graphcomponent.getSelection().getSelectedEdges().forEach(edge -> edge.setTag("filtered"));
		String start_date_str = text1.getText();
	    String end_date_str = text2.getText();
	    int start_date_int = Integer.parseInt(start_date_str.substring(0,4)+start_date_str.substring(5,7)+start_date_str.substring(8,10));
	    int end_date_int = Integer.parseInt(end_date_str.substring(0,4)+end_date_str.substring(5,7)+end_date_str.substring(8,10));
	    
	    for(int i =0 ; i<data_for_edge.length;i++) {
	    	int date = Integer.parseInt(data_for_edge[i][8].substring(0, 4)+data_for_edge[i][8].substring(5, 7)+data_for_edge[i][8].substring(8, 10));
	    	if(date<start_date_int || date>end_date_int) {
	    		edges[i].setTag("filtered");
	    	}
	    }
	 
	    // re-evaluate the filter predicates to actually hide the items
	    FilteredGraphWrapper filteredGraph = (FilteredGraphWrapper) graphcomponent.getGraph();
	    filteredGraph.nodePredicateChanged();
	    filteredGraph.edgePredicateChanged();

	    // enable the reset buttons
	    resetButton_for_edge.setEnabled(true);
	  }
	
	public void resetFilter() {
	    // access the unfiltered graph to remove the filter mark from all items
		  IGraph fullGraph = getFullGraph();

	    // unmark the selected items
	    fullGraph.getNodes().forEach(node -> node.setTag(null));
	    fullGraph.getEdges().forEach(edge -> edge.setTag(null));

	    // re-evaluate the filter predicates to actually show the items again
	    FilteredGraphWrapper filteredGraph = (FilteredGraphWrapper) graphcomponent.getGraph();
	    filteredGraph.nodePredicateChanged();
	    filteredGraph.edgePredicateChanged();

	    // disable the reset button
	    resetButton_for_edge.setEnabled(false);
	    resetButton_for_node.setEnabled(false);
	  }
	
	private IGraph getFullGraph() {
	    // the FilteredGraphWrapper is the current view graph:
	    FilteredGraphWrapper filteredGraph = (FilteredGraphWrapper) graphcomponent.getGraph();

	    // the full graph is the wrapped graph
	    IGraph fullGraph = filteredGraph.getWrappedGraph();
	    return fullGraph;
	  }
	
	protected JButton createTextButton( String tooltip, String text, ActionListener action ) {
	    JButton button = new JButton();
	    button.setToolTipText(tooltip);
	    button.setText(text);
	    button.addActionListener(action);
	    return button;
	  }
	
	
	//Design node
	public void DesignNode() {
		IGraph graph = graphcomponent.getGraph();
		ShapeNodeStyle nodeshape = new ShapeNodeStyle();
		nodeshape.setPaint(Colors.BLUE_VIOLET);
		//nodecolor.setPen(Pen.getTransparent());
		nodeshape.setShape(ShapeNodeShape.ELLIPSE);
		graph.getNodeDefaults().setStyle(nodeshape);
		
	}
	//Create node, edge, label
	public void CreateGraph() {
		IGraph graph = graphcomponent.getGraph();
		Random random = new Random();

		nodes = new INode[data_for_node.length];
		for(int i =0 ; i<data_for_node.length;i++) {
			int x = random.nextInt(3000)+50;
			int y = random.nextInt(1800)+50;
			nodes[i] = graph.createNode(new PointD(x,y));
		}
		
		
		ILabel [] node_labels = new ILabel[data_for_node.length];
		ExteriorLabelModel exteriorLabelModel = new ExteriorLabelModel();
		exteriorLabelModel.setInsets(new InsetsD(3));
		for(int i =0 ; i<data_for_node.length;i++) {
			node_labels[i] = graph.addLabel(nodes[i], "계좌번호 : "+data_for_node[i][2]);
			graph.setLabelLayoutParameter(node_labels[i], exteriorLabelModel.createParameter(ExteriorLabelModel.Position.SOUTH));
		}
		
		
		edges = new IEdge[data_for_edge.length];
		//System.out.println(data_for_edge.length);
		for(int i =0 ;i<data_for_edge.length;i++) {
			String start_acc = data_for_edge[i][2];
			String end_acc = data_for_edge[i][5];
			
			//System.out.println(start_acc+"\t"+end_acc);
			int start_index=0;
			int end_index=0;
			
			for(int j =0 ;j<data_for_node.length;j++) {
				//System.out.println(start_acc+"\t"+data_for_node[j][2]);
				if(start_acc.equals(data_for_node[j][2])) {
					start_index = j;
					//System.out.println("same");
					break;
				}
			}
			//System.out.println();
			for(int j =0 ;j<data_for_node.length;j++) {
				if(end_acc.equals(data_for_node[j][2])) {
					end_index = j;
				}
			}
			edges[i] = graph.createEdge(nodes[start_index],nodes[end_index]);
		}
		
		ILabel [] edge_labels = new ILabel[data_for_edge.length];
		EdgeSegmentLabelModel edgeSegmentLabelModel = new EdgeSegmentLabelModel();
		edgeSegmentLabelModel.setDistance(3);
		ILabelModelParameter labelModelParameter = edgeSegmentLabelModel.createParameterFromSource(0, 0.5, EdgeSides.RIGHT_OF_EDGE);
		graph.getEdgeDefaults().getLabelDefaults().setLayoutParameter(labelModelParameter);
		for(int i =0 ;i<data_for_edge.length;i++) {
			edge_labels[i] = graph.addLabel(edges[i], data_for_edge[i][7]);
		}
		/*
		//Create node
		//INode [] nodes = new INode[30];
		
		for(int i =0 ; i<nodes.length; i++) {
			int x = random.nextInt(800)+1;
			int y = random.nextInt(500)+1;
			nodes[i] = graph.createNode(new PointD(x,y));
		}
		
		//Create edge
		//IEdge [] edges = new IEdge[50];
		for(int i =0 ; i<nodes.length-1;i++) {
			edges[i] = graph.createEdge(nodes[i], nodes[i+1]);
		}
		int j = nodes.length-1;
		for(int i=0; i<21; i++) {
			edges[j] = graph.createEdge(nodes[random.nextInt(30)], nodes[random.nextInt(30)]);
			j++;
		}

		
		//Create node label
		ILabel [] nlabels = new ILabel[nodes.length];
		for(int i =0 ; i<nlabels.length;i++) {
			nlabels[i] = graph.addLabel(nodes[i], "Account"+i);
		}
		
		//Create edge label
		ILabel [] elabels = new ILabel[edges.length];
		for(int i =0 ;i<elabels.length;i++) {
			elabels[i] = graph.addLabel(edges[i], "edge"+i);
		}
*/
	}
	//Create Overview
	private JPanel createGraphOverview(){
		    JPanel graphOverviewContainer = new JPanel();
		    // Another GraphComponent that displays a small overview on the upper left corner of the application.
		    GraphOverviewComponent overviewComponent = new GraphOverviewComponent(graphcomponent);
		    overviewComponent.setMinimumSize(new Dimension(250, 250));
		    overviewComponent.setPreferredSize(new Dimension(250, 250));
		    graphOverviewContainer.add(overviewComponent);
		    graphOverviewContainer.setBorder(BorderFactory.createTitledBorder("Overview"));
		    return graphOverviewContainer;
		  }
	
	//Create NodeOrgChart
	public JPanel createNodeOrgChart() {
		JPanel contentPane = new JPanel(new GridLayout(1,1));
		JEditorPane editorPane = new JEditorPane();
		JScrollPane scollPane = new JScrollPane(editorPane);
		
		contentPane.add(scollPane);
		contentPane.setBorder(BorderFactory.createTitledBorder("계좌정보"));
		contentPane.setPreferredSize(new Dimension(250,180));
		
		//editorPane.setText("안녕하세요. \n서주안입니다.");
		graphcomponent.addCurrentItemChangedListener((sener,args) -> {
			int node_index=0;

			//System.out.println(graphcomponent.getCurrentItem());
			for(int i =0 ; i<data_for_node.length;i++) {
				if(graphcomponent.getCurrentItem()!=null) {
					if(graphcomponent.getCurrentItem().toString().substring(7).equals(data_for_node[i][2])) {
						current_node=i;
						node_index = i;
						break;
					}
				}
				else if(graphcomponent.getCurrentItem() == null){
					node_index = current_node;
					break;
				}
			}
			editorPane.setText("계좌번호 : "+data_for_node[node_index][2]+"\n\n은    행 : "+data_for_node[node_index][1]+"\n\n소유주  : "+data_for_node[node_index][0]);
		});

		editorPane.setEditable(false);
		return contentPane;
	}
	
	//Create EdgeOrgChart
	public JPanel createEdgeOrgChart() {
		JPanel contentPane = new JPanel(new GridLayout(1,1));
		JEditorPane editorPane = new JEditorPane();
		JScrollPane scollPane = new JScrollPane(editorPane);
		
		contentPane.add(scollPane);
		contentPane.setBorder(BorderFactory.createTitledBorder("입출금 정보"));
		contentPane.setPreferredSize(new Dimension(250,320));
		
		//editorPane.setText("안녕하세요. \n서주안입니다.");
		//GraphEditorInputMode mode = new GraphEditorInputMode();
	    mode.setSelectableItems(GraphItemTypes.NODE.or(GraphItemTypes.EDGE));
		
	    graphcomponent.setInputMode(mode);
	    mode.addItemClickedListener((sender,args) -> {
	    	if(args.getItem() instanceof IEdge) {
	    		int edge_index = 0;
	    		for(int i =0 ; i<data_for_edge.length;i++) {
					if(args.getItem().toString().equals(data_for_edge[i][7])) {
						edge_index = i;
						break;
					}
				}
	    		editorPane.setText("ROWID : "+args.getItem()+"\n\n출금계좌 : "+data_for_edge[edge_index][2] +"\n\n출금계좌 은행 : "+data_for_edge[edge_index][1]
	    				+"\n\n출금계좌 소유주 : " + data_for_edge[edge_index][0]+ "\n\n입금계좌 : " + data_for_edge[edge_index][5]+"\n\n입금계좌 은행 : "+data_for_edge[edge_index][4]
	    						+"\n\n입금계ㅔ좌 소유주 : "+data_for_edge[edge_index][3]+"\n\n금액 : "+data_for_edge[edge_index][6]+"\n\n일시 : "+data_for_edge[edge_index][8]);
	    	}
	    });
	    
	    
		editorPane.setEditable(false);
		return contentPane;
	}
	
	//Create RightPanel(Overview, NodeOrgChart, EdgeOrgchart)
	public JPanel createRightPanel() {
		JPanel rightpanel = new JPanel(new BorderLayout());
		rightpanel.add(createGraphOverview(),BorderLayout.NORTH);
		rightpanel.add(createNodeOrgChart(),BorderLayout.CENTER);
		rightpanel.add(createEdgeOrgChart(),BorderLayout.SOUTH);
		return rightpanel;
	}
	
	//Create Frame
	public void CreateFrame() {
		frame = new JFrame("TestMyself");
		frame.setSize(1200,800);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.add(graphcomponent,BorderLayout.CENTER);
		frame.add(toolbar,BorderLayout.NORTH);
		frame.add(createRightPanel(),BorderLayout.EAST);
		frame.setVisible(true);
	}
	
	public static void main(String[] args) throws IOException {
		SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build( Resources.getResourceAsReader("config/config.xml" ) );
		SqlSession session = sqlSessionFactory.openSession();
	//HashMap<String,Object> rs = session.selectOne("TRANS");
		List<HashMap<String,Object>> rs_node = session.selectList("TRANS_NODE");
		
		data_for_node = new String [rs_node.size()-1][3];

		for(int i =0 ;i<rs_node.size()-1;i++) {
			HashMap <String,Object> m_node = rs_node.get(i);
			
			if(m_node.get("name") !=null) {
				data_for_node[i][0] = m_node.get("name").toString();
			}
			else {
				data_for_node[i][0] = "NULL";
			}
			
			if(m_node.get("bank") !=null) {
				data_for_node[i][1] = m_node.get("bank").toString();
			}
			else {
				data_for_node[i][1] = "NULL";
			}
			
			if(m_node.get("acc") !=null) {
				data_for_node[i][2] = m_node.get("acc").toString();
			}
			else {
				data_for_node[i][2] = "NULL";
			}
			
		}
			
		List<HashMap<String,Object>> rs_edge = session.selectList("TRANS_EDGE");
		
		data_for_edge = new String [rs_edge.size()][9];
		
		for(int i =0 ;i<rs_edge.size();i++) {
			HashMap <String,Object> m_edge = rs_edge.get(i);
			
			if(m_edge.get("SRC_NAME") !=null) {
				data_for_edge[i][0] = m_edge.get("SRC_NAME").toString();
			}
			else {
				data_for_edge[i][0] = "NULL";
			}
			if(m_edge.get("SRC_BANK") !=null) {
				data_for_edge[i][1] = m_edge.get("SRC_BANK").toString();
			}
			else {
				data_for_edge[i][1] = "NULL";
			}
			if(m_edge.get("SRC_ACC") !=null) {
				data_for_edge[i][2] = m_edge.get("SRC_ACC").toString();
			}
			else {
				data_for_edge[i][2] = "NULL";
			}
			if(m_edge.get("DST_NAME") !=null) {
				data_for_edge[i][3] = m_edge.get("DST_NAME").toString();
			}
			else {
				data_for_edge[i][3] = "NULL";
			}
			if(m_edge.get("DST_BANK") !=null) {
				data_for_edge[i][4] = m_edge.get("DST_BANK").toString();
			}
			else {
				data_for_edge[i][4] = "NULL";
			}
			if(m_edge.get("DST_ACC") !=null) {
				data_for_edge[i][5] = m_edge.get("DST_ACC").toString();
			}
			else {
				data_for_edge[i][5] = "NULL";
			}
			if(m_edge.get("OUT_MONEY") !=null) {
				data_for_edge[i][6] = m_edge.get("OUT_MONEY").toString();
			}
			else {
				data_for_edge[i][6] = "NULL";
			}
			if(m_edge.get("ROWID") !=null) {
				data_for_edge[i][7] = m_edge.get("ROWID").toString();
			}
			else {
				data_for_edge[i][7] = "NULL";
			}
			if(m_edge.get("TR_DATE") !=null) {
				data_for_edge[i][8] = m_edge.get("TR_DATE").toString();
			}
			else {
				data_for_edge[i][8] = "NULL";
			}
			
		}
		session.close();
		TestMyself test = new TestMyself();
		test.Create();
		
	}

}




