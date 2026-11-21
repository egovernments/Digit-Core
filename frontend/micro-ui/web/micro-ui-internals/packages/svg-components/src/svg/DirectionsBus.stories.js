import React from "react";
import { DirectionsBus } from "./DirectionsBus";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DirectionsBus",
  component: DirectionsBus,
};

export const Default = () => <DirectionsBus />;
export const Fill = () => <DirectionsBus fill="blue" />;
export const Size = () => <DirectionsBus height="50" width="50" />;
export const CustomStyle = () => <DirectionsBus style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DirectionsBus className="custom-class" />;

export const Clickable = () => <DirectionsBus onClick={()=>console.log("clicked")} />;

const Template = (args) => <DirectionsBus {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
