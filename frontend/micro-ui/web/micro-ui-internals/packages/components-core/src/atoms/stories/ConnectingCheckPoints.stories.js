import React from "react";
import { CheckPoint, ConnectingCheckPoints } from "../ConnectingCheckPoints";

export default {
  title: "Atoms/ConnectingCheckPoints",
  component: ConnectingCheckPoints,
};

const Timeline = [
  {
    keyValue: 3,
    isCompleted: false,
    label: "Completed",
    key: 1,
    info: "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
  },
  {
    keyValue: 2,
    isCompleted: true,
    label: "Pending for DSO Assignment",
    key: 2,
    info: "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
  },
  {
    keyValue: 1,
    isCompleted: true,
    label: "Created",
    key: 3,
    info: "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
  },
];

export const Primary = () => {
  return (
    <ConnectingCheckPoints>
      {Timeline.map((checkpoint, index) => (
        <React.Fragment key={index}>
          <CheckPoint
            keyValue={index}
            isCompleted={index === 0}
            info={checkpoint?.info || "N/A"}
            label={checkpoint?.label || "N/A"}
          />
        </React.Fragment>
      ))}
    </ConnectingCheckPoints>
  );
};
