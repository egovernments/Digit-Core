import React from "react";
import { WorkspacesFilled } from "./WorkspacesFilled";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "WorkspacesFilled",
  component: WorkspacesFilled,
};

export const Default = () => <WorkspacesFilled />;
export const Fill = () => <WorkspacesFilled fill="blue" />;
export const Size = () => <WorkspacesFilled height="50" width="50" />;
export const CustomStyle = () => <WorkspacesFilled style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WorkspacesFilled className="custom-class" />;
export const Clickable = () => <WorkspacesFilled onClick={()=>console.log("clicked")} />;

const Template = (args) => <WorkspacesFilled {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

