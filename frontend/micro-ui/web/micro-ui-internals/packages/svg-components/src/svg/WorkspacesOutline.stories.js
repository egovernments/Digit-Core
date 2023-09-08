import React from "react";
import { WorkspacesOutline } from "./WorkspacesOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "WorkspacesOutline",
  component: WorkspacesOutline,
};

export const Default = () => <WorkspacesOutline />;
export const Fill = () => <WorkspacesOutline fill="blue" />;
export const Size = () => <WorkspacesOutline height="50" width="50" />;
export const CustomStyle = () => <WorkspacesOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WorkspacesOutline className="custom-class" />;
export const Clickable = () => <WorkspacesOutline onClick={()=>console.log("clicked")} />;

const Template = (args) => <WorkspacesOutline {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

