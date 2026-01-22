import React from "react";
import { AddModerator } from "./AddModerator";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AddModerator",
  component: AddModerator,
};

export const Default = () => <AddModerator />;
export const Fill = () => <AddModerator fill="blue" />;
export const Size = () => <AddModerator height="50" width="50" />;
export const CustomStyle = () => <AddModerator style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddModerator className="custom-class" />;
export const Clickable = () => <AddModerator onClick={()=>console.log("clicked")} />;

const Template = (args) => <AddModerator {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
