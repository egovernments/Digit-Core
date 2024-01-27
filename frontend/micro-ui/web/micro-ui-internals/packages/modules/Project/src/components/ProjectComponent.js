import React, { useState, useEffect } from "react";

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
              <td>
                <input
                  type="text"
                  value={formData[key] || ""}
                  onChange={(e) => props.onSelect(key, e.target.value)}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ProjectComponent;
