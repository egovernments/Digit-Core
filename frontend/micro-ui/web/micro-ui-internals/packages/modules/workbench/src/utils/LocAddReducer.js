export const intialState = {
  tableState: [],
};

const reducer = (state, action) => {
  switch (action.type) {
    case "ADD_ROW":
      const { state: newRow } = action;
      return { ...state, tableState: [...state.tableState, newRow] };
    case "UPDATE_ROW_KEYCODE":
      const {
        state: { id, value, row },
      } = action;
      const {tableState} = state

      let findIndex = tableState.findIndex(row => row[0].id === id)
      tableState[findIndex][0].code = value
      return { ...state, tableState };
    
    case "CLEAR_STATE":
      return { ...state, tableState: [] };
    case "clearSearchForm":
      return { ...state, searchForm: action.state };
    case "clearFilterForm":
      return { ...state, filterForm: action.state };
    
    default:
      return state;
  }
};

export default reducer;
