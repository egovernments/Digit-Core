import React from "react";
import { PrivateRoute } from "@egovernments/digit-ui-react-components";
import { Switch } from "react-router-dom";

const App = ({ path }) => {

  return (
    <Switch>
      <React.Fragment>
        <div>
          <PrivateRoute path={`${path}/sample`} component={() => <div>Sample Screen loaded</div>} />
        </div>
      </React.Fragment>
    </Switch>
  );
};

export default App;
