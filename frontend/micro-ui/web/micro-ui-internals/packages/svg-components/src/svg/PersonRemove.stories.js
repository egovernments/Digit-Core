import React from "react";
import { PersonRemove } from "./PersonRemove";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PersonRemove",
  component: PersonRemove,
};

export const Default = () => <PersonRemove />;
export const Fill = () => <PersonRemove fill="blue" />;
export const Size = () => <PersonRemove height="50" width="50" />;
export const CustomStyle = () => <PersonRemove style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PersonRemove className="custom-class" />;

export const Clickable = () => <PersonRemove onClick={()=>console.log("clicked")} />;

const Template = (args) => <PersonRemove {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
