import React from "react";

const ProjectComponent = (props) => {
  const { formData } = props;

  return (
    <div>
      <h3>ProjectComponent</h3>
      <table className="sample-component-style">
        <thead>
          <tr>
            <th>Key</th>
            <th>Value</th>
          </tr>
        </thead>
        <tbody>
          {Object.keys(formData).map((key) => (
            <tr key={key}>
              <td>{key}</td>
              <td>{JSON.stringify(formData[key])}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ProjectComponent;
