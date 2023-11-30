export const intialState = {
  tableState: [],
};

const reducer = (state, action) => {
  switch (action.type) {
    case "ADD_ROW":
      const { state: newRow } = action;
      return { ...state, tableState: [...state.tableState, newRow] };
    case "UPDATE_ROW":
      const {
        state: { id, value, row ,type},
      } = action;
      const {tableState} = state
      let findIndex = tableState.findIndex(row => row.id === id)
      if(type==="keycode") tableState[findIndex].code = value
      if(type==="message") tableState[findIndex].message = value
      return { ...state, tableState };
    
    case "DELETE_ROW":
      const indexToDelete = action.state.row.index
      state.tableState.splice(indexToDelete,1) 
      return state
    case "CLEAR_STATE":
      return { ...state, tableState: [] };
    
    default:
      return state;
  }
};

export default reducer;
